package com.boojet.boot_api.controllers;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

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

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.services.TransactionService;


@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }


    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        return transactionService.addTransaction(transaction);
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.findAllTransactions();
    }
    @GetMapping("/{id}")
    public Transaction getOne(@PathVariable Long id) {
        return transactionService.findTransaction(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with ID " + id + " not found."));
    }

    @PutMapping("/{id}")
    public Transaction updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {

        if(!transactionService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with ID " + id + " not found.");
        }

        return transactionService.updateTransaction(id, transaction);
    }

    @PatchMapping("/{id}")
    public Transaction patchTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {

        if(!transactionService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with ID " + id + " not found.");
        }

        return transactionService.updateTransaction(id, transaction);
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        if(!transactionService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with ID " + id + " not found.");
        }

        transactionService.delete(id);
    }

    //--------------------------------Filters / Reports---------------------------------------------

    @GetMapping("/category/{cat}")
    public List<Transaction> byCategory(@PathVariable Category cat){
        return transactionService.findTransactionsByCategory(cat);
    }

    @GetMapping("/month/{year}/{month}")
    public List<Transaction> byMonth(@PathVariable int year, @PathVariable int month){
        return transactionService.findTransactionsByMonth(YearMonth.of(year, month));
    }

    //Controller should have no knowledge of Money
    @GetMapping("/balance")
    public Money balance(){
        return transactionService.calculateTotalBalance();
    }

    @GetMapping("summary/{year}/{month}")
    public Map<Category, Money> monthlySummary(@PathVariable int year, @PathVariable int month){
        List<Transaction> transactionsInMonth = transactionService.findTransactionsByMonth(YearMonth.of(year, month));
        return transactionService.summariseByCategory(transactionsInMonth);
    }

    

}
