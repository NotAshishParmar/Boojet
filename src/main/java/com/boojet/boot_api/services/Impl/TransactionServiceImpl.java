package com.boojet.boot_api.services.Impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.CategoryType;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.dto.category.CategorySummaryDto;
import com.boojet.boot_api.dto.transaction.TransactionCreateRequest;
import com.boojet.boot_api.dto.transaction.TransactionPatchRequest;
import com.boojet.boot_api.dto.transaction.TransactionPutRequest;
import com.boojet.boot_api.dto.transaction.TxSuggestionDetails;
import com.boojet.boot_api.exceptions.AccountNotFoundException;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.CategoryNotFoundException;
import com.boojet.boot_api.exceptions.TransactionNotFoundException;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.CategoryRepository;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.repositories.projections.CategoryTotalView;
import com.boojet.boot_api.services.TransactionService;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    //private final AccountServiceImpl accountRepositoryImpl;

    // Dependency injection of the repository
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    // ----------------------------CRUD operations----------------------------------

    // add a new transaction and return the saved entity
    @Override
    @Transactional                                            //this annotation allows Spring to rollback and avoid partial writes to DB in case of an early termination due to exceptions
    public Transaction createTransaction(TransactionCreateRequest req) {

        if(req == null) throw new BadRequestException("Request must not be null");

        requirePositive(req.amount());

        Category cat = resolveCategoryId(req.categoryId());
        Account from = resolveAccountId(req.accountId());

        LocalDate date = (req.date() != null) ? req.date() : LocalDate.now();

        Transaction tx = Transaction.builder()
                .description(normalizeDescription(req.description()))
                .amount(req.amount())
                .date(date)
                .category(cat)
                .account(from)
                .toAccount(resolveToAccountId(req.toAccountId()))
                .build();

        applyDerivedAndTransferRules(tx);

        return transactionRepository.save(tx);
    }

    // return a list of all transactions
    @Deprecated
    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();

    }

    // search transactions with optional filters and pagination
    // functions as findAll if no filters are provided (CRUD Read)
    @Override
    public Page<Transaction> search(Long accountId, Long categoryId, Integer year, Integer month, Pageable pageable) {
        
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

        return transactionRepository.search(accId, categoryId, from, to, pageable);
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
    public Transaction putTransaction(Long id, TransactionPutRequest req) {

        validateTransactionId(id);
        if(req == null) throw new BadRequestException("Request must not be null");

        requirePositive(req.amount());

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        Category cat = resolveCategoryId(req.categoryId());
        Account from = resolveAccountId(req.accountId());
        Account to = resolveToAccountId(req.toAccountId());

        if(req.date() == null) throw new BadRequestException("date is required");

        existing.setDescription(normalizeDescription(req.description()));
        existing.setAmount(req.amount());
        existing.setDate(req.date());
        existing.setCategory(cat);
        existing.setAccount(from);
        existing.setToAccount(to);

        applyDerivedAndTransferRules(existing);

        return transactionRepository.save(existing);
    }



    // update an existing transaction by its ID and return the updated entity
    @Override
    @Transactional
    public Transaction patchTransaction(Long id, TransactionPatchRequest req) {

        validateTransactionId(id);
        if(req == null) throw new BadRequestException("Request must not be null");

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        if(req.description() != null) existing.setDescription(normalizeDescription(req.description()));
        if(req.amount() != null){
            validatePositiveIfPresent(req.amount());
            existing.setAmount(req.amount());
        }
        if(req.date() != null) existing.setDate(req.date());

        if(req.categoryId() != null) existing.setCategory(resolveCategoryId(req.categoryId()));
        if(req.accountId() != null) existing.setAccount(resolveAccountId(req.accountId()));

        applyToAccountPatch(existing, req.toAccountId());

        // after all changes, enforce final semantics
        applyDerivedAndTransferRules(existing);

        return transactionRepository.save(existing);
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

        if(name == null)
            return List.of();

        String trim = name.trim();

        if(trim.length() < 2)
            return List.of();

        if(howMany <= 0)
            return List.of();

        int limit = Math.min(howMany, 15);

        //LinkedHashSet keeps the order of insertions and keeps duplicates in check (dedupes)
        Set<String> out = new LinkedHashSet<>(limit);

        //get prefixes first
        List<String> prefix = transactionRepository.suggestPrefix(trim, PageRequest.of(0, limit));
        out.addAll(prefix);

        //exit early if limit already reached
        if(out.size() >= limit){
            return new ArrayList<>(out).subList(0, limit);
        }

        //fetch extra for contains so deduping doesn't starve results
        int containsFetch = Math.min(limit*2, 30);

        List<String> contains = transactionRepository.suggestContains(trim, PageRequest.of(0, containsFetch));

        for(String s : contains){
            out.add(s);
            if(out.size() >= limit)
                break;
        }

        return new ArrayList<>(out);

    }

    @Override
    public TxSuggestionDetails suggestionDetails(String description){
        
        if(description == null)
            throw new BadRequestException("Description for transaction suggestion cannot be null");

        String trimDescription = description.trim();

        if(trimDescription.isEmpty())
            throw new BadRequestException("Description for transaction suggestion cannot be blank");

        if(trimDescription.length() < 2)
            throw new BadRequestException("Description cannot be less than 2 characters (Tx suggestion)");
        
        Transaction match = transactionRepository.findTopByDescriptionIgnoreCaseOrderByDateDescIdDesc(trimDescription).orElseThrow(
            () -> new TransactionNotFoundException("No transactions match the description provided (Tx Suggestion)")
        );
        
        return new TxSuggestionDetails(match.getDescription(), match.getCategory().getId(), match.getAmount(), match.isIncome(), match.getAccount().getId());
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
        return transactionRepository.search(null, category.getId(), 
            LocalDate.of(1,1,1), LocalDate.of(9999,12,31), pageable);
    }

    @Override
    public Money calculateTotalByCategory(Category category) {
        if (category == null) throw new BadRequestException("Category must not be null");
        BigDecimal net = transactionRepository.sumNetForCategory(category.getId());
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

        return rows.stream()
                .map(r -> new CategorySummaryDto(r.getCategory(), Money.of(r.getTotal())))
                .toList();
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


    private void applyDerivedAndTransferRules(Transaction tx){

        CategoryType type = tx.getCategory().getType();

        if(type == CategoryType.TRANSFER){

            if(tx.getToAccount() == null){
                throw new BadRequestException("Transfer transactions must include toAccountId");
            }

            if(tx.getAccount().getId().equals(tx.getToAccount().getId())){
                throw new BadRequestException("Transfer source and destination accounts must be different");
            }

            tx.setIncome(false);
            return;
        }

        // non-transfer
        tx.setToAccount(null);
        tx.setIncome(type == CategoryType.INCOME);
    }

    private void applyToAccountPatch(Transaction existing, JsonNode toAccountId){

        // missing -> no change
        if(toAccountId == null) return;

        if(toAccountId.isNull()){
            existing.setToAccount(null); // explicit clear
            return;
        }

        if(toAccountId.isNumber()){
            existing.setToAccount(validateAccount(toAccountId.asLong()));
            return;
        }

        throw new BadRequestException("toAccountId must be a number or null");
    }

    private void validateTransactionId(Long id) {
        if(id == null || id <= 0){
            throw new BadRequestException("Transaction ID must be a positive number");
        }
    }

    private String normalizeDescription(String desc){
        if(desc == null || desc.isBlank())
            return "No description";
        return desc.trim();
    }

    private void requirePositive(Money amount){
        if(amount == null || !amount.isPositive())
            throw new BadRequestException("Transaction amount must be provided and be a positive value");
    }

    private void validatePositiveIfPresent(Money amount){
        if(amount != null && !amount.isPositive())
            throw new BadRequestException("Transaction amount must be a positive value");
    }

    private Account resolveAccountId(Long accountId){
        if(accountId == null)
            throw new BadRequestException("Account id is required");
        return validateAccount(accountId);
    }

    private Account resolveToAccountId(Long accountId){
        if(accountId == null)
            return null;
        return validateAccount(accountId);
    }

    private Category resolveCategoryId(Long categoryId){
        if(categoryId == null)
            throw new BadRequestException("Category ID is required");
        return validateCategory(categoryId);
    }

    private Account validateAccount(Long accountId){

        if(accountId == null || accountId <= 0)
            throw new BadRequestException("Account ID must be a positive number");

        return accountRepository.findById(accountId)
                .orElseThrow(()-> new AccountNotFoundException(accountId));
    }

    private Category validateCategory(Long categoryId){

        if(categoryId == null || categoryId <= 0)
            throw new BadRequestException("Category ID must be a positive number");

        return categoryRepository.findById(categoryId)
                .orElseThrow(()-> new CategoryNotFoundException(categoryId));
    }

    private YearMonth buildYearMonthOrThrow(int year, int month){
        try{
            return YearMonth.of(year, month);
        }catch(RuntimeException e){
            throw new BadRequestException("Cannot build YearMonth. Invalid Year/Month Transaction");
        }
    }

}
