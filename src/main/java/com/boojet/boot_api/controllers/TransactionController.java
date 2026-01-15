package com.boojet.boot_api.controllers;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import com.boojet.boot_api.controllers.dto.CategorySummaryDto;
import com.boojet.boot_api.controllers.dto.TransactionDto;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.mappers.Mapper;

import com.boojet.boot_api.services.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Transaction")
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    private TransactionService transactionService;
    private Mapper<Transaction, TransactionDto> transactionMapper;


    public TransactionController(TransactionService transactionService, Mapper<Transaction, TransactionDto> transactionMapper){
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @Operation(summary = "Create a new transaction", description = "Creates a new transaction with the provided details.")
    @PostMapping
    public TransactionDto createTransaction(@RequestBody TransactionDto transactionDto) {

        Transaction transaction = transactionMapper.mapFrom(transactionDto);
        Transaction savedTransaction = transactionService.addTransaction(transaction);
        return transactionMapper.mapTo(savedTransaction);
    }

    // @GetMapping
    // public List<TransactionDto> getAllTransactions() {
    //     List<Transaction> transactions = transactionService.findAllTransactions();
    //     return transactions.stream()
    //             .map(transaction -> transactionMapper.mapTo(transaction))
    //             .toList();
    // }

    @Operation(summary = "Search transactions", description = "Search for transactions based on optional filters such as account ID, category, year, and month. Supports pagination.")
    @GetMapping
    public PageResponse<TransactionDto> search(@RequestParam(required = false) Long accountId,
                                    @RequestParam(required = false) Category category,
                                    @RequestParam(required = false) Integer year,
                                    @RequestParam(required = false) Integer month,
                                    @ParameterObject
                                    @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<TransactionDto> page = transactionService.search(accountId, category, year, month, pageable).map(transactionMapper::mapTo);

        return PageResponse.of(page);
        
    }
    
    @Operation(summary = "Get transaction by ID", description = "Retrieve a specific transaction by its ID.")
    @GetMapping("/{id}")
    public TransactionDto getOne(@PathVariable Long id) {

        Transaction transaction = transactionService.findTransaction(id);
        return transactionMapper.mapTo(transaction);
    }

    @Operation(summary = "Update a transaction by ID", description = "Update the details of an existing transaction by its ID.")
    @PutMapping("/{id}")
    public TransactionDto updateTransaction(@PathVariable Long id, @RequestBody TransactionDto transactionDto) {

        Transaction updatedTransaction = transactionService.updateTransactionComplete(id, transactionMapper.mapFrom(transactionDto));
        return transactionMapper.mapTo(updatedTransaction);
    }

    @Operation(summary = "Partially update a transaction by ID", description = "Partially update the details of an existing transaction by its ID.")
    @PatchMapping("/{id}")
    public TransactionDto patchTransaction(@PathVariable Long id, @RequestBody TransactionDto transactionDto) {

        Transaction patchedTransaction =  transactionService.updateTransaction(id, transactionMapper.mapFrom(transactionDto));
        return transactionMapper.mapTo(patchedTransaction);
    }

    @Operation(summary = "Delete a transaction by ID", description = "Delete an existing transaction by its ID.")
    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        if(!transactionService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction with ID " + id + " not found.");
        }

        transactionService.delete(id);
    }

    //--------------------------------Filters / Reports---------------------------------------------

    @Operation(summary = "Get transactions by category", description = "Retrieve a list of transactions filtered by the specified category.")
    @GetMapping("/category/{cat}")
    public PageResponse<TransactionDto> byCategory(@PathVariable Category cat, 
                                            @ParameterObject
                                            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable){

        Page<TransactionDto> transactions = transactionService.findTransactionsByCategory(cat, pageable).
                                                map(transactionMapper::mapTo);

        return PageResponse.of(transactions);

    }

    @Operation(summary = "Get transactions by month", description = "Retrieve a list of transactions for the specified year and month.")
    @GetMapping("/month/{year}/{month}")
    public PageResponse<TransactionDto> byMonth(@PathVariable int year, @PathVariable int month,
                                    @ParameterObject
                                    @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable){

        Page<TransactionDto> transactions = transactionService.findTransactionsByMonth(year, month, pageable).
                                        map(transactionMapper::mapTo);

        return PageResponse.of(transactions);
    }

    @Operation(summary = "Get total balance", description = "Calculate and retrieve the total balance from all transactions.")
    @GetMapping("/balance")
    public Money balance(){
        return transactionService.calculateTotalBalance();
    }

    @Operation(summary = "Get monthly summary by category", description = "Retrieve a summary of transactions for a specific month, grouped by category.")
    @GetMapping("/summary/{year}/{month}")
    public List<CategorySummaryDto> monthlySummary(@PathVariable int year, @PathVariable int month){
        return transactionService.monthlySummaryByCategory(year, month);
    }
}
