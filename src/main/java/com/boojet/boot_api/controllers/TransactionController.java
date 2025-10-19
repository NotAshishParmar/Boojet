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

import com.boojet.boot_api.controllers.dto.TransactionDto;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.mappers.Mapper;
import com.boojet.boot_api.services.TransactionService;


@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    private TransactionService transactionService;
    private Mapper<Transaction, TransactionDto> transactionMapper;


    public TransactionController(TransactionService transactionService, Mapper<Transaction, TransactionDto> transactionMapper){
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }


    @PostMapping
    public TransactionDto createTransaction(@RequestBody TransactionDto transactionDto) {

        Transaction transaction = transactionMapper.mapFrom(transactionDto);
        Transaction savedTransaction = transactionService.addTransaction(transaction);
        return transactionMapper.mapTo(savedTransaction);
    }

    @GetMapping
    public List<TransactionDto> getAllTransactions() {
        List<Transaction> transactions = transactionService.findAllTransactions();
        return transactions.stream()
                .map(transaction -> transactionMapper.mapTo(transaction))
                .toList();
    }

    @GetMapping("/{id}")
    public TransactionDto getOne(@PathVariable Long id) {

        Transaction transaction = transactionService.findTransaction(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with ID " + id + " not found."));
        return transactionMapper.mapTo(transaction);
    }

    @PutMapping("/{id}")
    public TransactionDto updateTransaction(@PathVariable Long id, @RequestBody TransactionDto transactionDto) {

        if(!transactionService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with ID " + id + " not found.");
        }

        Transaction updatedTransaction = transactionService.updateTransaction(id, transactionMapper.mapFrom(transactionDto));
        return transactionMapper.mapTo(updatedTransaction);
    }

    @PatchMapping("/{id}")
    public TransactionDto patchTransaction(@PathVariable Long id, @RequestBody TransactionDto transactionDto) {

        if(!transactionService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with ID " + id + " not found.");
        }

        Transaction patchedTransaction =  transactionService.updateTransaction(id, transactionMapper.mapFrom(transactionDto));
        return transactionMapper.mapTo(patchedTransaction);
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
    public List<TransactionDto> byCategory(@PathVariable Category cat){

        List<Transaction> transactions = transactionService.findTransactionsByCategory(cat);

        return transactions.stream()
            .map(transaction -> transactionMapper.mapTo(transaction))
            .toList();

    }

    @GetMapping("/month/{year}/{month}")
    public List<TransactionDto> byMonth(@PathVariable int year, @PathVariable int month){

        List<Transaction> transactions = transactionService.findTransactionsByMonth(YearMonth.of(year, month));

        return transactions.stream()
            .map(transaction -> transactionMapper.mapTo(transaction))
            .toList();
    }

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
