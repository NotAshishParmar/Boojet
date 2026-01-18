package com.boojet.boot_api.services.Impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.AccountType;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.domain.ValidationMode;
import com.boojet.boot_api.exceptions.AccountNotFoundException;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.UserRepository;
import com.boojet.boot_api.services.AccountService;
import com.boojet.boot_api.services.TransactionService;


@Service
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepo;
    private final UserRepository userRepo;
    private final TransactionService transactionService;

    private static final Long DEFAULT_USER_ID = 1L; //temporary until user management is implemented
    private static int DEFAULT_COUNTER = 1;


    public AccountServiceImpl(AccountRepository accountRepo, UserRepository userRepo, TransactionService transactionService){
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
        this.transactionService = transactionService;
    }

    //----------------------CRUD----------------------------
    @Override
    @Transactional
    public Account createAccount(Account account) {
        if(account.getUser() == null){
            User defaultUser = userRepo.getReferenceById(DEFAULT_USER_ID);
            account.setUser(defaultUser);
        }

        applyCreateDefaults(account);
        Account verifiedAccount = validateAccount(account, ValidationMode.CREATE);

        return accountRepo.save(verifiedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAllAccounts() {
        return accountRepo.findAll();
    }

    @Override
    @Transactional
    public Account findAccount(Long id) {

        validateAccountId(id);

        return accountRepo.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    @Transactional
    public Account updateAccountComplete(Long id, Account account){
        validateAccountId(id);
        Account verifiedAccount = validateAccount(account, ValidationMode.PUT_FULL);
        return updateAccount(id, verifiedAccount);
    }

    @Override
    @Transactional
    public Account updateAccount(Long id, Account account) {

        validateAccountId(id);
        Account verifiedAccount = validateAccount(account, ValidationMode.PATCH_PARTIAL);
        verifiedAccount.setId(id);

        return accountRepo.findById(id).map(existingAccount -> {

            LocalDate effectiveCreatedAt = 
                verifiedAccount.getCreatedAt() != null 
                    ? verifiedAccount.getCreatedAt() 
                    : existingAccount.getCreatedAt();

            if (verifiedAccount.getClosedAt() != null 
                    && effectiveCreatedAt != null 
                    && verifiedAccount.getClosedAt().isBefore(effectiveCreatedAt)) {
                throw new BadRequestException("Account cannot be closed prior to creation");
            }

            Optional.ofNullable(verifiedAccount.getUser()).ifPresent(existingAccount::setUser);
            Optional.ofNullable(verifiedAccount.getName()).ifPresent(existingAccount::setName);
            Optional.ofNullable(verifiedAccount.getType()).ifPresent(existingAccount::setType);
            Optional.ofNullable(verifiedAccount.getOpeningBalance()).ifPresent(existingAccount::setOpeningBalance);
            Optional.ofNullable(verifiedAccount.getCreatedAt()).ifPresent(existingAccount::setCreatedAt);
            Optional.ofNullable(verifiedAccount.getClosedAt()).ifPresent(existingAccount::setClosedAt);
            return accountRepo.save(existingAccount);
        }).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        validateAccountId(id);

        if(!accountRepo.existsById(id)){
            throw new AccountNotFoundException(id);
        }
        accountRepo.deleteById(id);
    }

    //------------------------------------------------------//

    @Override
    @Transactional(readOnly = true)
    public boolean isExists(Long id){
        return id != null && id > 0 && accountRepo.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Money balance(Long id){

        validateAccountId(id);
        Account acct = accountRepo.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id));

        Money delta = transactionService.calculateTotalByAccount(acct);
        return acct.getOpeningBalance().add(delta);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> listAllTransactionsInAccount(Long id, Pageable pageable){

        validateAccountId(id);
        if (!accountRepo.existsById(id)) {
            throw new AccountNotFoundException(id);
        }
        return transactionService.search(id, null, null, null, pageable);
    }

    //-------------------------------------------------helpers------------------------------------------------------------
    
    private void validateAccountId(Long id){
        if(id == null || id <= 0){
            throw new BadRequestException("Account must have a valid positive Id");
        }
    }

    private Account validateAccount(Account account, ValidationMode mode){ 
        
        if(account == null){
            throw new BadRequestException("Account must not be null");
        }

        final boolean requireAll = (mode != ValidationMode.PATCH_PARTIAL);

        // --- Required fields for CREATE / PUT_FULL ---
        if (requireAll) {
            if (account.getUser() == null || account.getUser().getId() == null) {
                throw new BadRequestException("Account must be associated with a valid user");
            }
            if (account.getName() == null || account.getName().isBlank()) {
                throw new BadRequestException("Account name is required");
            }
            if (account.getType() == null) {
                throw new BadRequestException("Account type is required");
            }
            if (account.getOpeningBalance() == null) {
                throw new BadRequestException("Opening balance is required");
            }

        }else{
            // PATCH: if user is provided, id must exist
            if (account.getUser() != null && account.getUser().getId() == null) {
                throw new BadRequestException("If user is provided, it must contain a valid id");
            }
        }

        // --- Normalize optional inputs ---
        if (account.getName() != null) {
            account.setName(account.getName().trim());
        }

        // --- Temporal rules ---
        if (account.getCreatedAt() != null && account.getCreatedAt().isAfter(LocalDate.now())) {
            throw new BadRequestException("Account cannot be created in the future");
        }

        // If both dates are present in the payload, validate relative order here.
        // (If PATCH and createdAt is not present, also validate after you load existing in the service.)
        if (account.getClosedAt() != null && account.getCreatedAt() != null
                && account.getClosedAt().isBefore(account.getCreatedAt())) {
            throw new BadRequestException("Account cannot be closed prior to creation");
        }

        return account;

    }

    private void applyCreateDefaults(Account account){
        if(account.getName() == null || account.getName().isBlank()){
            account.setName("Default Account " + DEFAULT_COUNTER);
            DEFAULT_COUNTER++;
        }

        if(account.getType() == null){
            account.setType(AccountType.CHEQUING);
        }

        if(account.getOpeningBalance() == null){
            account.setOpeningBalance(Money.zero());
        }

        if(account.getCreatedAt() == null){
            account.setCreatedAt(LocalDate.now());
        }
    }

}
