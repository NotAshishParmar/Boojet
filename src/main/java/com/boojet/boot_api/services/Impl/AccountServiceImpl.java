package com.boojet.boot_api.services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.repositories.UserRepository;
import com.boojet.boot_api.services.AccountService;

@Service
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepo;
    private final UserRepository userRepo;
    private final TransactionRepository transactionRepo;

    private static final Long DEAFULT_USER_ID = 1L; //temporary until user management is implemented


    public AccountServiceImpl(AccountRepository accountRepo, UserRepository userRepo, TransactionRepository transactionRepo){
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
    }

    //----------------------CRUD----------------------------
    @Override
    public Account createAccount(Account account) {
        if(account.getUser() == null){
            User defaultUser = userRepo.getReferenceById(DEAFULT_USER_ID);
            account.setUser(defaultUser);
        }

        return accountRepo.save(account);
    }

    @Override
    public List<Account> findAllAccounts() {
        return accountRepo.findAll();
    }

    @Override
    public Optional<Account> findAccount(Long id) {
        return accountRepo.findById(id);
    }

    @Override
    public Account updateAccount(Long id, Account account) {
        account.setId(id);

        return accountRepo.findById(id).map(existingAccount -> {
            Optional.ofNullable(account.getUser()).ifPresent(existingAccount::setUser);
            Optional.ofNullable(account.getName()).ifPresent(existingAccount::setName);
            Optional.ofNullable(account.getType()).ifPresent(existingAccount::setType);
            Optional.ofNullable(account.getOpeningBalance()).ifPresent(existingAccount::setOpeningBalance);
            Optional.ofNullable(account.getCreatedAt()).ifPresent(existingAccount::setCreatedAt);
            Optional.ofNullable(account.getClosedAt()).ifPresent(existingAccount::setClosedAt);
            return accountRepo.save(existingAccount);
        }).orElseThrow(() -> new RuntimeException("Account not found with id " + id));
    }

    @Override
    public void delete(Long id) {
        accountRepo.deleteById(id);
    }

    //------------------------------------------------------//

    @Override
    public boolean isExists(Long id){
        return accountRepo.existsById(id);
    }

    @Override
    public Money balance(Long id){
        Account acct = accountRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account " + id + " not found"));

        Money delta = transactionRepo.findByAccountIdOrderByDateDesc(id).stream()
                .map(t -> t.isIncome() ? t.getAmount() : t.getAmount().negate())
                .reduce(Money.zero(), Money::add);

        return acct.getOpeningBalance().add(delta);
    }

    @Override
    public List<Transaction> listAllTransactionsInAccount(Long id){
        if (!accountRepo.existsById(id)) {
            throw new IllegalArgumentException("Account " + id + " not found");
        }
        return transactionRepo.findByAccountIdOrderByDateDesc(id);
    }
    

}
