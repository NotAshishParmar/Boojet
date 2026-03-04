package com.boojet.boot_api.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boojet.boot_api.controllers.dto.BalanceSnapshotRequest;
import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.AccountBalanceSnapshot;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.services.AccountBalanceSnapshotService;
import com.boojet.boot_api.services.AccountService;
import com.boojet.boot_api.services.BalanceService;
import com.boojet.boot_api.services.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Account")
@RestController
@RequestMapping("/account")
public class AccountController {

    private AccountService accountService;
    private TransactionService transactionService;
    private BalanceService balanceService;
    private AccountBalanceSnapshotService snapshotService;

    public AccountController(AccountService accountService, TransactionService transactionService, BalanceService balanceService, AccountBalanceSnapshotService snapshotService){
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.balanceService = balanceService;
        this.snapshotService = snapshotService;
    }

    //-------------------------------------------------CRUD-------------------------------------------------------

    @Operation(summary = "Create a new account", description = "Creates a new account with the provided details.")
    @PostMapping
    public Account createAccount(@RequestBody Account account) {          
        return accountService.createAccount(account);
    }

    @Operation(summary = "Get all accounts", description = "Retrieve a list of all accounts.")
    @GetMapping
    public List<Account> getAllAccounts(){
        return accountService.findAllAccounts();
    }

    @Operation(summary = "Get an account by ID", description = "Retrieve the details of an account by its ID.")
    @GetMapping("/{id}")
    public Account getOne(@PathVariable Long id){
        Account account = accountService.findAccount(id);
        return account;
    }

    @Operation(summary = "Update an account by ID", description = "Update the details of an existing account by its ID.")
    @PutMapping("/{id}")
    public Account updateAccount(@PathVariable Long id, @RequestBody Account account){
        Account updatedAccount = accountService.updateAccountComplete(id, account);
        return updatedAccount;
    }

    @Operation(summary = "Partially update an account by ID", description = "Partially update the details of an existing account by its ID.")
    @PatchMapping("/{id}")
    public Account patchAccount(@PathVariable Long id, @RequestBody Account account){
        Account patchedAccount = accountService.updateAccount(id, account);
        return patchedAccount;
    }

    @Operation(summary = "Delete an account by ID", description = "Delete an existing account by its ID.")
    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id){
        accountService.delete(id);
    }

    //---------------------------------------------Reports / Calculations---------------------------------------------

    @Operation(summary = "Get account balance by ID", description = "Calculate and retrieve the balance for a specific account by its ID.")
    @GetMapping("/balance/{id}")
    public ResponseEntity<?> balance(@PathVariable Long id) {
        LocalDate current = LocalDate.now().plusDays(1);

        return balanceService.getBalanceAsOf(id, current)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(422).body(
                Map.of(
                    "error", "NO_SNAPSHOT",
                    "message", "No balance snapshot exists for this account yet.",
                    "accountId", id
                )
            ));
    }

    @Operation(summary = "List balance snapshots for account", description = "Returns all saved balance snapshots for an account, newest first.")
    @GetMapping("/{id}/balance-snapshots")
    public List<AccountBalanceSnapshot> listBalanceSnapshots(@PathVariable Long id) {
        return snapshotService.listSnapshots(id);
    }

    @Operation(summary = "Upsert account balance snapshot", description = "Creates or updates the balance snapshot for the account on the given date asOfDate (defaults to today).")
    @PutMapping("/{id}/balance-snapshot")
    public AccountBalanceSnapshot upsertBalanceSnapshot(@PathVariable Long id, @RequestBody BalanceSnapshotRequest req){
        return snapshotService.upsert(id, req.asOfDate(), req.balance());
    }

    @Operation(summary = "Get account balance as of date", description = "Returns the account balance at the start of the given date (defaults to today).")
    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getBalanceAsOf(@PathVariable Long id, @RequestParam(required = false) LocalDate asOf){
        LocalDate target = (asOf == null) ? LocalDate.now().plusDays(1) : asOf;

        return balanceService.getBalanceAsOf(id, target)
                                .<ResponseEntity<?>>map(b -> ResponseEntity.ok(Map.of("accountId", id, "asOf", target, "balance", b)))
                                .orElseGet(() -> ResponseEntity.status(422).body(
                                    Map.of("error",  "NO_SNAPSHOT", "message", "No balance snapshot exists for this account yet.",
                                            "accountId", id, "asOf", target)                                
                            ));
    }

    // Secondary Search, uses the TransactionService to get page of transactions for the account
    @Operation(summary = "Get transactions for an account by ID", description = "Retrieve a paginated list of transactions associated with a specific account by its ID.")
    @GetMapping("/{id}/transactions")
    public Page<Transaction> byAccount(@PathVariable Long id, @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.search(id, null, null, null, pageable);
    }

}
