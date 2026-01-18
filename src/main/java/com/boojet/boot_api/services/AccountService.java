package com.boojet.boot_api.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.AccountNotFoundException;

/**
 * Service contract for managing {@link Account} records.
 * This service defines the business-level operations for creating and managing
 * accounts in Boojet. Implementations are responsible for validating input
 * and persisting accounts through the repository layer.
 * 
 * <b>Notes:</b>
 * <ul>
 * <li>Callers should provide a valid {@link Account} that meets the
 * applications constraints.</li>
 * <li>Accounts are associated with a {@link User} (owner).</li>
 * </ul>
 */
public interface AccountService {

    //-------------CRUD-------------------//

    /**
     * Create and persist a new {@link Account}.
     * 
     * <ul>
     *  <li>{@code account} must not be null.</li>
     *  <li>Each {@code account} must be associated with a {@link User}. If a user
     *  is not provided then a default user is assigned.</li>
     *  <li>If {@code account.name} is {@code null} or blank, it is set to
     *  {@code "DEFAULT ACCOUNT X"} where X is the default account number. </li>
     *  <li>If {@code account.type} is {@code null}, it is set to 
     *  {@code AccountType.CHEQUING}.</li>
     *  <li>If {@code account.openingBalance} is {@code null}, it is set to
     *  {@code Money.zero()}.</li>
     *  <li>If {@code account.createdAt} is {@code null}, it is set to 
     *  {@code LocalDate.now()}.</li>
     *  <li>If {@code account.closedAt} is provided, then it must be after  
     *  {@code account.createdAt} but before {@code LocalDate.now()}.</li>
     * </ul>
     * 
     * Runs within a transactional context to ensure data integrity. On any runtime
     * exception, the account is rolled back (no partial writes).
     * 
     * @param account the candidate account to add
     * @return the saved account with a generated ID and any defaults applied
     * @throws BadRequestException if input fails validation
     */
    Account createAccount(Account account);

    /**
     * Finds and returns a list of all {@code Account(s)} present in the repository.
     * 
     * @return A {@code List} of all accounts created
     */
    List<Account> findAllAccounts();

    /**
     * Finds a {@link Account} by its unique ID.
     * 
     * @param id the ID of the account to find
     * @return the found account
     * @throws BadRequestException if the provided ID is not positive or is {@code null}
     * @throws AccountNotFoundException if no account with the given ID exists
     */
    Account findAccount(Long id);

    /**
     * Updates an existing {@link Account} by replacing all fields with those
     * from the provided {@code account}.
     * 
     * <ul>
     *  <li>All fields in {@code account} are used to update the existing
     *  record.</li>
     *  <li>The {@code id} field in {@code account} is ignored; the provided
     *  {@code id} parameter is used to locate the existing record.</li>
     *  <li>If no existing record is found with the given {@code id}, an
     *  {@link AccountNotFoundException} is thrown.</li>
     * </ul>
     * 
     * 
     * @param id the ID of the account to update
     * @param account the account data to update with (all fileds used)
     * @return the updated account
     * @throws AccountNotFoundException if no account with the given ID exists
     * @throws BadRequestException if input is {@code null} or fails validation
     */
    Account updateAccountComplete(Long id, Account account);

    /**
     * Partially updates an existing {@link Account} with the non-null fields
     * from the provided {@code account}.
     * 
     * <ul>
     *  <li>Only non-null fields in {@code account} are used to update the
     *  existing record.</li>
     *  <li>The {@code id} field in {@code account} is ignored; the provided
     *  {@code id} parameter is used to locate the existing record.</li>
     *  <li>If no existing record is found with the given {@code id}, an
     *  {@link AccountNotFoundException} is thrown.</li>
     * </ul>
     * 
     * 
     * @param id the ID of the account to update
     * @param account the account data to update with (only non-null fields used)
     * @return the updated account
     * @throws AccountNotFoundException if no account with the given ID exists
     * @throws BadRequestException if input fails validation
     */
    Account updateAccount(Long id, Account account);

    /**
     * Deletes the {@link Account} with the given ID.
     * 
     * @param id the ID of the account to delete
     * @throws AccountNotFoundException if no account with the given ID exists
     * @throws BadRequestException if the provided ID is not positive or is {@code null}
     */
    void delete(Long id);
    //------------------------------------//

    /**
     * Check if an {@link Account} exists by its ID.
     * 
     * 
     * @param id the ID of the account to check
     * @return {@code true} if an account with the given ID exists, {@code false} otherwise. Invalid IDs return false.
     */
    boolean isExists(Long id);

    /**
     * Calculates the total balance from all transactions associated
     * with the provided account {@code id}.
     * 
     * @return the total balance as a {@link Money} object
     */
    Money balance(Long id);

    /**
     * Finds and returns a {@code Page} of all transactions that are associated with the
     * provided account {@code id}.
     * 
     * @param id the ID of the account to filter transactions by; must not be {@code null}
     * @param pageable pagination information
     * @return a page of transactions that are associated with the provided account {@code id}
     * @throws BadRequestException if the provided {@code id} is {@code null}
     * @throws AccountNotFoundException if the provided account does not exist in database
     */
    Page<Transaction> listAllTransactionsInAccount(Long id, Pageable pageable);
}
