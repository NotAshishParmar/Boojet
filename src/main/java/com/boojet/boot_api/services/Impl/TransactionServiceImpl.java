package com.boojet.boot_api.services.Impl;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.services.TransactionService;


@Service
public class TransactionServiceImpl implements TransactionService {

    // Dependency injection of the repository
    private TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    // ----------------------------CRUD operations----------------------------------

    //add a new transaction and return the saved entity
    @Override
    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    //return a list of all transactions
    @Override
    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();
        
    }

    //return a transaction by its ID
    @Override
    public Optional<Transaction> findTransaction(Long id) {
        return transactionRepository.findById(id);
    }

    //update an existing transaction by its ID and return the updated entity
    @Override
    public Transaction updateTransaction(Long id, Transaction transaction) {
        // Ensure the transaction to update has the correct ID
        transaction.setId(id);

        return transactionRepository.findById(id).map(existingTransaction -> {
            Optional.ofNullable(transaction.getDescription()).ifPresent(existingTransaction::setDescription);
            Optional.ofNullable(transaction.getAmount()).ifPresent(existingTransaction::setAmount);
            Optional.ofNullable(transaction.getDate()).ifPresent(existingTransaction::setDate);
            Optional.ofNullable(transaction.getCategory()).ifPresent(existingTransaction::setCategory);
            Optional.ofNullable(transaction.isIncome()).ifPresent(existingTransaction::setIncome);
            return transactionRepository.save(existingTransaction);
        }).orElseThrow(() -> new RuntimeException ("Transaction not found with id " + id));
    }

    //delete a transaction by its ID
    @Override
    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }
    
    // -----------------------------------------------------------------------------

    //check if a transaction exists by its ID
    @Override
    public boolean isExists(Long id) {
        return transactionRepository.existsById(id);
    }

    //calculate the total balance from all transactions
    @Override
    public Money calculateTotalBalance() {

        //TIP: could be optimized with a custom query in the repository to calculate the sum directly in the database
        //but for simplicity, we'll do it in memory here
        List<Transaction> transactions = transactionRepository.findAll();
        Money balance = Money.zero();

        for(Transaction t : transactions){
            balance = balance.add(t.isIncome()
                    ? t.getAmount()
                    : t.getAmount().negate());
        }

        return balance;
    }

    @Override
    public List<Transaction> findTransactionsByMonth(YearMonth ym) {
        return transactionRepository.findAll().stream()
                                    .filter(t -> YearMonth.from(t.getDate()).equals(ym))
                                    .toList();
    }

    @Override
    public Money calculateMonthlyBalance(YearMonth ym) {
        List<Transaction> transactions = findTransactionsByMonth(ym);
        Money balance = Money.zero();

        for(Transaction t : transactions){
            balance = balance.add(t.isIncome()
                    ? t.getAmount()
                    : t.getAmount().negate());
        }
        return balance;
    }

    @Override
    public List<Transaction> findTransactionsByCategory(Category category) {
        return transactionRepository.findAll().stream()
                                    .filter(t -> t.getCategory() == category)
                                    .toList();
        
    }

    @Override
    public Money calculateTotalByCategory(Category category) {
        List<Transaction> transactions = findTransactionsByCategory(category);
        Money balance = Money.zero();

        for(Transaction t : transactions){
            balance = balance.add(t.isIncome()
                    ? t.getAmount()
                    : t.getAmount().negate());
        }

        return balance;
    }

    @Override
    public Map<Category, Money> summariseByCategory(List<Transaction> transactions) {
        return transactions.stream().collect(
            Collectors.groupingBy(Transaction::getCategory,
                                    Collectors.mapping(Transaction::getAmount,
                                        Collectors.reducing(Money.zero(), (a,b)-> a.add(b)))));
    }

}
