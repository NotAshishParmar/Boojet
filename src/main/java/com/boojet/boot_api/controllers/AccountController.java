package com.boojet.boot_api.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.services.AccountService;
import com.boojet.boot_api.services.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Account")
@RestController
@RequestMapping("/account")
public class AccountController {

    private AccountService accountService;
    private TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService){
        this.accountService = accountService;
        this.transactionService = transactionService;
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
        if(!accountService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with ID " + id + " not found.");
        }

        Account updatedAccount = accountService.updateAccount(id, account);
        return updatedAccount;
    }

    @Operation(summary = "Partially update an account by ID", description = "Partially update the details of an existing account by its ID.")
    @PatchMapping("/{id}")
    public Account patchAccount(@PathVariable Long id, @RequestBody Account account){
        if(!accountService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with ID " + id + " not found.");
        }

        Account patchedAccount = accountService.updateAccount(id, account);
        return patchedAccount;
    }

    @Operation(summary = "Delete an account by ID", description = "Delete an existing account by its ID.")
    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id){
        if(!accountService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Account with ID " + id + " not found.");
        }

        accountService.delete(id);
    }

    //---------------------------------------------Reports / Calculations---------------------------------------------

    @Operation(summary = "Get account balance by ID", description = "Calculate and retrieve the balance for a specific account by its ID.")
    @GetMapping("/balance/{id}")
    public Money balance(@PathVariable Long id) {
        return accountService.balance(id);
    }

    // Secondary Search, uses the TransactionService to get page of transactions for the account
    @Operation(summary = "Get transactions for an account by ID", description = "Retrieve a paginated list of transactions associated with a specific account by its ID.")
    @GetMapping("/{id}/transactions")
    public Page<Transaction> byAccount(@PathVariable Long id, @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.search(id, null, null, null, pageable);
    }

}
