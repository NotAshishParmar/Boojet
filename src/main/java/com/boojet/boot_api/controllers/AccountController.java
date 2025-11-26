package com.boojet.boot_api.controllers;

import java.util.List;

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



@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    //CRUD
    @PostMapping
    public Account createAccount(@RequestBody Account account) {          
        return accountService.createAccount(account);
    }

    @GetMapping
    public List<Account> getAllAccounts(){
        return accountService.findAllAccounts();
    }

    @GetMapping("/{id}")
    public Account getOne(@PathVariable Long id){
        Account account = accountService.findAccount(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with ID " + id + " not found"));

        return account;
    }

    @PutMapping("/{id}")
    public Account updateAccount(@PathVariable Long id, @RequestBody Account account){
        if(!accountService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with ID " + id + " not found.");
        }

        Account updatedAccount = accountService.updateAccount(id, account);
        return updatedAccount;
    }

    @PatchMapping("/{id}")
    public Account patchAccount(@PathVariable Long id, @RequestBody Account account){
        if(!accountService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with ID " + id + " not found.");
        }

        Account patchedAccount = accountService.updateAccount(id, account);
        return patchedAccount;
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id){
        if(!accountService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Account with ID " + id + " not found.");
        }

        accountService.delete(id);
    }

    @GetMapping("/balance/{id}")
    public Money balance(@PathVariable Long id) {
        return accountService.balance(id);
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> listTransactionsInAccount(@PathVariable Long id) {
        return accountService.listAllTransactionsInAccount(id);
    }
    
}
