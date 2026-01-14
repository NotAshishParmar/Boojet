package com.boojet.boot_api.services.Impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.domain.ValidationMode;
import com.boojet.boot_api.exceptions.AccountNotFoundException;
import com.boojet.boot_api.exceptions.BadRequestException;
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
    @Transactional                                            //this annotation allows Spring to rollback and avoid partial writes to DB in case of an early termination due to exceptions
    public Transaction addTransaction(Transaction transaction) {

        //verify transaction data (all fields including Account) or throw
        Transaction verifiedTransaction = validateTransaction(transaction, ValidationMode.CREATE);

        applyCreateDefaults(verifiedTransaction);


        return transactionRepository.save(verifiedTransaction);
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
    public Transaction findTransaction(Long id) {

        validateTransactionId(id);

        //findById returns an Optional<Transaction> so we unwrap it or throw it, which gives us Transaction
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        return transaction;
    }

    @Override
    @Transactional
    public Transaction updateTransactionComplete(Long id, Transaction transaction) {

        Transaction verifiedTransaction = validateTransaction(transaction, ValidationMode.PUT_FULL);
        verifiedTransaction.setId(id);
        return updateTransaction(id, verifiedTransaction);
    }



    // update an existing transaction by its ID and return the updated entity
    @Override
    @Transactional
    public Transaction updateTransaction(Long id, Transaction transaction) {

        //throws BadRequestException if id is null or not positive
        validateTransactionId(id);

        //attach verified account or throw
        if(transaction.getAccount() != null && transaction.getAccount().getId() != null){
            Account account = transaction.getAccount();
            Account verifiedAccount = validateAccount(account.getId());
            transaction.setAccount(verifiedAccount);
        }

        // Ensure the transaction to update has the correct ID
        transaction.setId(id);

        return transactionRepository.findById(id).map(existingTransaction -> {
            Optional.ofNullable(transaction.getDescription()).ifPresent(existingTransaction::setDescription);
            Optional.ofNullable(transaction.getAmount()).ifPresent(existingTransaction::setAmount);
            Optional.ofNullable(transaction.getDate()).ifPresent(existingTransaction::setDate);
            Optional.ofNullable(transaction.getCategory()).ifPresent(existingTransaction::setCategory);
            Optional.ofNullable(transaction.isIncome()).ifPresent(existingTransaction::setIncome);
            return transactionRepository.save(existingTransaction);
        }).orElseThrow(() -> new TransactionNotFoundException(id));
    }

    // delete a transaction by its ID
    @Override
    public void delete(Long id) {

        //throws BadRequestException if id is null or not positive
        validateTransactionId(id);

        //throw if not found
        if(!transactionRepository.existsById(id)){
            throw new TransactionNotFoundException(id);
        }

        transactionRepository.deleteById(id);
    }

    // -----------------------------------------------------------------------------

    // check if a transaction exists by its ID
    @Override
    @Transactional(readOnly = true)
    public boolean isExists(Long id) {
        return id != null && id > 0 && transactionRepository.existsById(id);
    }

    // calculate the total balance from all transactions
    @Override
    @Transactional(readOnly = true)
    public Money calculateTotalBalance() {
        BigDecimal net = transactionRepository.sumNetAll(); // never null due to COALESCE
        return Money.of(net);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByMonth(YearMonth ym) {
        if (ym == null) throw new BadRequestException("YearMonth must not be null");
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        return transactionRepository.findByDateBetweenOrderByDateDesc(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public Money calculateMonthlyBalance(YearMonth ym) {
        if (ym == null) throw new BadRequestException("YearMonth must not be null");
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        BigDecimal net = transactionRepository.sumNetBetween(start, end);
        return Money.of(net);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByCategory(Category category) {
        if (category == null) throw new BadRequestException("Category must not be null");
        return transactionRepository.findByCategoryOrderByDateDesc(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Money calculateTotalByCategory(Category category) {
        if (category == null) throw new BadRequestException("Category must not be null");
        BigDecimal net = transactionRepository.sumNetByCategory(category);
        return Money.of(net);
    }

    @Override
    public Map<Category, Money> summariseByCategory(List<Transaction> transactions) {
        return transactions.stream().collect(
            Collectors.groupingBy(
                Transaction::getCategory,
                () -> new EnumMap<>(Category.class),
                Collectors.mapping(
                    t -> t.isIncome() ? t.getAmount() : t.getAmount().negate(), // sign!
                    Collectors.reducing(Money.zero(), Money::add)
                )
            )
        );
    }



    //---------------------------------Helpers--------------------------------------

    private void validateTransactionId(Long id) {
        if(id == null || id <= 0){
            throw new BadRequestException("Transaction ID must be a positive number");
        }
    }

    private Transaction validateTransaction(Transaction transaction, ValidationMode mode) {

        if (transaction == null) {
            throw new BadRequestException("Transaction must not be null");
        }

        Money amount = transaction.getAmount();

        //amount must be positive if provided, required for CREATE & PUT_FULL
        if((mode != ValidationMode.PATCH_PARTIAL && amount == null) ||
           (amount != null && !amount.isPositive())){
            throw new BadRequestException("Transaction amount must be provided and be a positive value");
        }

        if(mode != ValidationMode.PATCH_PARTIAL && transaction.getCategory() == null){
            throw new BadRequestException("Transaction category must be provided");
        }

        if(mode != ValidationMode.PATCH_PARTIAL && (transaction.getAccount() == null || transaction.getAccount().getId() == null)){
            throw new BadRequestException("Transaction must be associated with an existing account");
        }

        // if client provides an account with an id in any mode, verify it exists and attach verified account OR throw
        if(transaction.getAccount() != null && transaction.getAccount().getId() != null){
            transaction.setAccount(validateAccount(transaction.getAccount().getId()));
        }


        return transaction;
    }

    private Transaction applyCreateDefaults(Transaction transaction) {
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }

        if (transaction.getDescription() == null || transaction.getDescription().isBlank()) {
            transaction.setDescription("No description");
        }

        return transaction;
    }

    private Account validateAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
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
