package com.boojet.boot_api.services.Impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.exceptions.AccountNotFoundException;
import com.boojet.boot_api.exceptions.TransactionNotFoundException;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.services.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {

    //private final AccountServiceImpl accountServiceImpl;

    // Dependency injection of the repository
    private TransactionRepository transactionRepository;

    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    // ----------------------------CRUD operations----------------------------------

    // add a new transaction and return the saved entity
    @Override
    public Transaction addTransaction(Transaction transaction) {

        //null check
        if (transaction == null)
            throw new IllegalArgumentException("Transaction must not be null");

        //validate required fields
        if(transaction.getAccount() == null || transaction.getAccount().getId() == null){
            throw new IllegalArgumentException("Transaction must be associated with a valid Account");
        }

        if(transaction.getAmount() == null){
            throw new IllegalArgumentException("Transaction amount must not be null");
        }

        if(transaction.getDate() == null){
            transaction.setDate(LocalDate.now());
        }

        if(transaction.getDescription() == null || transaction.getDescription().isBlank()){
            transaction.setDescription("No description");
        }

        if(transaction.getCategory() == null){
            throw new IllegalArgumentException("Transaction category must not be null");
        }

        if(transaction.getAmount().isNegative() || transaction.getAmount().isZero()){
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        
        if(!accountRepository.existsById(transaction.getAccount().getId())){
            throw new AccountNotFoundException(transaction.getAccount().getId());
        }

        return transactionRepository.save(transaction);
    }

    // return a list of all transactions
    @Deprecated
    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();

    }

    // search transactions with optional filters and pagination
    // functions as findAll if no filters are provided (CRUD Read)
    @Override
    public Page<Transaction> search(Long accountId, Category category, YearMonth yearMonth, Pageable pageable) {
        if (accountId != null && !accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        LocalDate from = (yearMonth != null) ? yearMonth.atDay(1) : LocalDate.of(1, 1, 1);
        LocalDate to = (yearMonth != null) ? yearMonth.atEndOfMonth() : LocalDate.of(9999, 12, 31);

        return transactionRepository.search(accountId, category, from, to, pageable);
    }

    // return a transaction by its ID
    @Override
    public Optional<Transaction> findTransaction(Long id) {
        return transactionRepository.findById(id);
    }

    // update an existing transaction by its ID and return the updated entity
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
        }).orElseThrow(() -> new RuntimeException("Transaction not found with id " + id));
    }

    // delete a transaction by its ID
    @Override
    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }

    // -----------------------------------------------------------------------------

    // check if a transaction exists by its ID
    @Override
    public boolean isExists(Long id) {
        return transactionRepository.existsById(id);
    }

    // calculate the total balance from all transactions
    @Override
    public Money calculateTotalBalance() {

        // TIP: could be optimized with a custom query in the repository to calculate
        // the sum directly in the database
        // but for simplicity, we do it in memory here
        List<Transaction> transactions = transactionRepository.findAll();
        Money balance = Money.zero();

        for (Transaction t : transactions) {
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

        for (Transaction t : transactions) {
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

        for (Transaction t : transactions) {
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
                                Collectors.reducing(Money.zero(), (a, b) -> a.add(b)))));
    }

    // @Override
    // public List<Transaction> findTransactionsByAccount(Account account) {
    // return transactionRepository.findAll().stream()
    // .filter(t -> t.getAccount() == account)
    // .toList();
    // }

    // @Override
    // public Money calculateAccountBalance(Account account) {
    // List<Transaction> transactions = findTransactionsByAccount(account);
    // Money balance = Money.zero();

    // for(Transaction t : transactions){
    // balance = balance.add(t.isIncome()
    // ? t.getAmount()
    // : t.getAmount().negate());
    // }

    // return balance;
    // }

}
