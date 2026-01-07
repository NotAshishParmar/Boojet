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

public interface TransactionService {
    
    // CRUD operations
    Transaction addTransaction(Transaction transaction);
    // List<Transaction> findAllTransactions();
    Page<Transaction> search(Long accountId, Category category, YearMonth ym, Pageable pageable);
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
