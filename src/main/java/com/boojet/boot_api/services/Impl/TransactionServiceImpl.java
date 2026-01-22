package com.boojet.boot_api.services.Impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.controllers.dto.CategorySummaryDto;
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
import com.boojet.boot_api.repositories.projections.CategoryTotalView;
import com.boojet.boot_api.services.TransactionService;

@Service
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    //private final AccountServiceImpl accountRepositoryImpl;

    // Dependency injection of the repository
    private final TransactionRepository transactionRepository;

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

        applyCreateDefaults(transaction);

        //verify transaction data (all fields including Account) or throw
        Transaction verifiedTransaction = validateTransaction(transaction, ValidationMode.CREATE);

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
    public Page<Transaction> search(Long accountId, Category category, Integer year, Integer month, Pageable pageable) {
        
        Long accId = null;
        if (accountId != null) {
            if (!accountRepository.existsById(accountId)) {
                throw new AccountNotFoundException(accountId);
            }
            accId = accountId; // safe to pass down
        }

        //try to build yearMonth, throw if invalid input
        YearMonth yearMonth = (year != null && month != null) ? buildYearMonthOrThrow(year, month) : null;

        LocalDate from = (yearMonth != null) ? yearMonth.atDay(1) : LocalDate.of(1, 1, 1);
        LocalDate to = (yearMonth != null) ? yearMonth.atEndOfMonth() : LocalDate.of(9999, 12, 31);

        return transactionRepository.search(accId, category, from, to, pageable);
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
    @Transactional
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
    public boolean isExists(Long id) {
        return id != null && id > 0 && transactionRepository.existsById(id);
    }

    @Override
    public List<String> suggest(String name, int howMany){

        //returns null if input name is empty
        if(name == null)
            return List.of();

        String trimmed = name.trim();

        //do not return a full list of suggestions for 0 or 1 letter words
        if(trimmed.length() < 2)
            return List.of();

        //user wants no input sure
        if(howMany <= 0)
            return List.of();

        if(howMany > 15)
            howMany = 15;

        


        return null;
    }

    // calculate the total balance from all transactions
    @Override
    public Money calculateTotalBalance() {
        BigDecimal net = transactionRepository.sumNetAll(); // never null due to COALESCE
        return Money.of(net);
    }

    @Override
    public Page<Transaction> findTransactionsByMonth(Integer year, Integer month, Pageable pageable) {

        if(year == null || month == null){
            throw new BadRequestException("Year or month cannot be null");
        }

        YearMonth ym = buildYearMonthOrThrow(year, month);

        if (ym == null) throw new BadRequestException("YearMonth must not be null");
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        return transactionRepository.search(null, null, start, end, pageable);
    }

    @Override
    public Money calculateMonthlyBalance(Integer year, Integer month) {
        if(year == null || month == null){
            throw new BadRequestException("Year or month cannot be null");
        }

        YearMonth ym = buildYearMonthOrThrow(year, month);

        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        BigDecimal net = transactionRepository.sumNetBetween(start, end);
        return Money.of(net);
    }

    @Override
    public Page<Transaction> findTransactionsByCategory(Category category, Pageable pageable) {
        if (category == null) throw new BadRequestException("Category must not be null");
        return transactionRepository.search(null, category, 
            LocalDate.of(1,1,1), LocalDate.of(9999,12,31), pageable);
    }

    @Override
    public Money calculateTotalByCategory(Category category) {
        if (category == null) throw new BadRequestException("Category must not be null");
        BigDecimal net = transactionRepository.sumNetByCategory(category);
        return Money.of(net);
    }

    @Override
    public Money calculateTotalByAccount(Account account){
        if(account == null){
            throw new BadRequestException("Account must not be null");
        }

        Account verifiedAccount = validateAccount(account.getId());

        return Money.of(transactionRepository.sumNetForAccount(verifiedAccount.getId()));
    }

    @Override
    public List<CategorySummaryDto> monthlySummaryByCategory(int year, int month) {
        
        YearMonth ym = buildYearMonthOrThrow(year, month);

        List<CategoryTotalView> rows =  transactionRepository.sumNetByCategoryBetween(ym.atDay(1), ym.atEndOfMonth());

        Map<Category, BigDecimal> byCat = rows.stream()
            .collect(Collectors.toMap(
                CategoryTotalView::getCategory,
                CategoryTotalView::getTotal
            ));

        ArrayList<CategorySummaryDto> result = new ArrayList<CategorySummaryDto>();

        for(Category c: Category.values()){
            BigDecimal total = byCat.getOrDefault(c, BigDecimal.ZERO);
            result.add(new CategorySummaryDto(c, Money.of(total)));
        }

        return result;
    }

    @Override
    public Money calculateIncomeBetween(LocalDate start, LocalDate end){

        if(start == null || end == null){
            throw new BadRequestException("Date range cannot be null");
        }

        return Money.of(transactionRepository.sumIncomeBetween(start, end));
    }

    @Override 
    public Money calculateExpensesBetween(LocalDate start, LocalDate end){
        if(start == null || end == null){
            throw new BadRequestException("Date range cannot be null");
        }

        return Money.of(transactionRepository.sumExpensesBetween(start, end));
    }



    //---------------------------------------------------Helpers-----------------------------------------------------------------

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

        if(mode != ValidationMode.PATCH_PARTIAL && transaction.getDate() == null){
            throw new BadRequestException("Date of transaction cannot be null");
        }

        if(mode != ValidationMode.PATCH_PARTIAL && (transaction.getDescription() == null || transaction.getDescription().isBlank())){
            throw new BadRequestException("Transaction description must be provided");
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
        if (accountId == null || accountId <= 0) {
            throw new BadRequestException("Account ID must be a positive number");
        }
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private YearMonth buildYearMonthOrThrow(int year, int month){
        try{
            return YearMonth.of(year, month);
        }catch(RuntimeException e){
            throw new BadRequestException("Cannot build YearMonth. Invalid Year/Month Transaction");
        }
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
