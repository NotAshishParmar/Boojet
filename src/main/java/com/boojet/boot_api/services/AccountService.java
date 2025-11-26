package com.boojet.boot_api.services;

import java.util.List;
import java.util.Optional;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;

public interface AccountService {

    //-------------CRUD-------------------//
    Account createAccount(Account account);
    List<Account> findAllAccounts();
    Optional<Account> findAccount(Long id);
    Account updateAccount(Long id, Account account);
    void delete(Long id);
    //------------------------------------//

    boolean isExists(Long id);

    Money balance(Long id);
    List<Transaction> listAllTransactionsInAccount(Long id);
}
