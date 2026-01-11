package com.boojet.boot_api.services;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;

/**
 * Service contract for managing {@link Transaction} records.
 * This service defines the business-level operations for creating and managing 
 * transactions in Boojet. Implementations are responsible for validating input
 * and persisting transactions through the repository layer.
 * 
 * <b>Notes:</b>
 * <ul>
 *  <li>Callers should provide a valid {@link Transaction} that meets the applications constraints. </li>
 *  <li>Adding a transaction, updates the related account balance and total balance on the ledger</li>
 * </ul>
 */
public interface TransactionService {
    
    //--------------CRUD operations----------------

    /**
     * Pesists a new transaction.
     * 
     * The provided {@code transaction} must be valid (all required fields present).
     * 
     * @param transaction the transaction to add (must not be {@code null})
     * @return the saved transaction with an assigned id
     * @throws IllegalArgumentException if {@code transaction} is {@code null} or fails validation
     */
    Transaction addTransaction(Transaction transaction);
    // List<Transaction> findAllTransactions();
    /**
     * Searches the ledger for {@code Page(s)} of transactions that are associated 
     * with the provided {@code accountId}, {@code category} or {@code yearMonth}. 
     * Returns all transactions of none are provided (findAll)
     * 
     * @param accountId
     * @param category
     * @param yearMonth
     * @param pageable
     * @return {@code Page} of all transactions based on the input
     * 
     */
    Page<Transaction> search(Long accountId, Category category, YearMonth yearMonth, Pageable pageable);
    Optional<Transaction> findTransaction(Long id);
    Transaction updateTransaction(Long id, Transaction transaction);
    void delete(Long id);

    //helpers
    boolean isExists(Long id);

    //business logic
    Money calculateTotalBalance();
    List<Transaction> findTransactionsByMonth(YearMonth ym);
    Money calculateMonthlyBalance(YearMonth ym);
    List<Transaction> findTransactionsByCategory(Category category);
    Money calculateTotalByCategory(Category category);
    Map<Category, Money> summariseByCategory(List<Transaction> transactions);
}
