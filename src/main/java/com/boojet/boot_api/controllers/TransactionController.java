package com.boojet.boot_api.controllers;

import java.net.URI;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boojet.boot_api.controllers.dto.CategorySummaryDto;
import com.boojet.boot_api.controllers.dto.TransactionCreateRequest;
import com.boojet.boot_api.controllers.dto.TransactionPatchRequest;
import com.boojet.boot_api.controllers.dto.TransactionPutRequest;
import com.boojet.boot_api.controllers.dto.TransactionResponse;
import com.boojet.boot_api.controllers.dto.TxSuggestionDetails;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.mappers.Impl.TransactionMapper;
import com.boojet.boot_api.services.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Transaction")
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    private TransactionService transactionService;
    private TransactionMapper transactionMapper;


    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper){
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @Operation(summary = "Create a new transaction", description = "Creates a new transaction with the provided details.")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionCreateRequest req) {

        Transaction transaction = transactionService.createTransaction(req);
        TransactionResponse response = transactionMapper.mapTo(transaction);
        
        URI location = URI.create("/transactions/" + transaction.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Search transactions", description = "Search for transactions based on optional filters such as account ID, category, year, and month. Supports pagination.")
    @GetMapping
    public PageResponse<TransactionResponse> search(@RequestParam(required = false) Long accountId,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) Integer year,
                                    @RequestParam(required = false) Integer month,
                                    @ParameterObject
                                    @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<TransactionResponse> page = transactionService.search(accountId, categoryId, year, month, pageable)
                                                        .map(transactionMapper::mapTo);

        return PageResponse.of(page);
        
    }
    
    @Operation(summary = "Get transaction by ID", description = "Retrieve a specific transaction by its ID.")
    @GetMapping("/{id}")
    public TransactionResponse getOne(@PathVariable Long id) {
        Transaction transaction = transactionService.findTransaction(id);
        return transactionMapper.mapTo(transaction);
    }

    @Operation(summary = "Update a transaction by ID", description = "Update the details of an existing transaction by its ID.")
    @PutMapping("/{id}")
    public TransactionResponse updateTransaction(@PathVariable Long id, @RequestBody TransactionPutRequest req) {
        return transactionMapper.mapTo(transactionService.putTransaction(id, req));
    }

    @Operation(summary = "Partially update a transaction by ID", description = "Partially update the details of an existing transaction by its ID.")
    @PatchMapping("/{id}")
    public TransactionResponse patchTransaction(@PathVariable Long id, @RequestBody TransactionPatchRequest req) {
        return transactionMapper.mapTo(transactionService.patchTransaction(id, req));
    }

    @Operation(summary = "Delete a transaction by ID", description = "Delete an existing transaction by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build(); //204 no content
    }

    //--------------------------------Filters / Reports---------------------------------------------

    @Operation(summary = "Get transactions by category", description = "Retrieve a list of transactions filtered by the specified category.")
    @GetMapping("/category/{cat}")
    public PageResponse<TransactionResponse> byCategory(@PathVariable Category cat, 
                                            @ParameterObject
                                            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable){

        Page<TransactionResponse> transactions = transactionService.findTransactionsByCategory(cat, pageable).
                                                map(transactionMapper::mapTo);

        return PageResponse.of(transactions);

    }

    @Operation(summary = "Get transactions by month", description = "Retrieve a list of transactions for the specified year and month.")
    @GetMapping("/month/{year}/{month}")
    public PageResponse<TransactionResponse> byMonth(@PathVariable int year, @PathVariable int month,
                                    @ParameterObject
                                    @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable){

        Page<TransactionResponse> transactions = transactionService.findTransactionsByMonth(year, month, pageable).
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

    //TODO: add userId to this endpoint once Auth has been implemented
    @Operation(summary = "Get a list of suggested descriptions", description = "Returns a list of transactions descriptions that corresponds to the input String. Helps autofill names for that user")
    @GetMapping("/suggestions")
    public List<String> suggestDescription(@RequestParam String description,
                                            @RequestParam(defaultValue = "10") int howMany){
        return transactionService.suggest(description, howMany);
    }

    @Operation(summary = "Get Transaction details that match input", description = "Returns a record of Transaction category, amount, income, account that match the description input. Useful for autofill")
    @GetMapping("/suggestions/details")
    public TxSuggestionDetails suggestDetails(@RequestParam String description) {
        return transactionService.suggestionDetails(description);
    }
}
