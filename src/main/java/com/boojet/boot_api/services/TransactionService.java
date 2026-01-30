package com.boojet.boot_api.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.boojet.boot_api.controllers.dto.CategorySummaryDto;
import com.boojet.boot_api.controllers.dto.TxSuggestionDetails;
import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.CategoryEnum;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.exceptions.AccountNotFoundException;
import com.boojet.boot_api.exceptions.TransactionNotFoundException;
import com.boojet.boot_api.exceptions.BadRequestException;

/**
 * Service contract for managing {@link Transaction} records.
 * This service defines the business-level operations for creating and managing
 * transactions in Boojet. Implementations are responsible for validating input
 * and persisting transactions through the repository layer.
 * 
 * <b>Notes:</b>
 * <ul>
 * <li>Callers should provide a valid {@link Transaction} that meets the
 * applications constraints.</li>
 * <li>Adding a transaction, updates the related account balance and total
 * balance on the ledger</li>
 * </ul>
 */
public interface TransactionService {

    // --------------CRUD operations----------------

    /**
     * Creates and persists a new {@link Transaction}.
     * 
     * <ul>
     *  <li>{@code transaction} must not be {@code null}.</li>
     *  <li>{@code transaction.account.id} is required (the account must exist in DB).</li>
     *  <li>{@code transaction.amount} must be positive.</li>
     *  <li>{@code transaction.category} is required.</li>
     *  <li>If {@code transaction.date} is {@code null}, it is set to
     *  {@code LocalDate.now()}.</li>
     *  <li>If {@code transaction.description} is {@code null} or blank, it is set to
     *  {@code "No description"}.</li>
     * </ul>
     * 
     * 
     * Runs within a transactional context to ensure data integrity. On any runtime
     * exception, the transaction is rolled back (no partial writes).
     * 
     * @param transaction the candidate transaction to add
     * @return the saved transaction with generated ID and any defaults applied
     * @throws BadRequestException if input is {@code null} or fails validation
     * @throws AccountNotFoundException if the associated account does not exist
     */
    Transaction addTransaction(Transaction transaction);

    // List<Transaction> findAllTransactions();

    /**
     * Searches the ledger for {@code Page(s)} of transactions that are associated
     * with the provided {@code accountId}, {@code category} or {@code yearMonth}.
     * Returns all transactions if none are provided (findAll)
     * 
     * Use {@link org.springframework.data.domain.Pageable} to control pagination and sorting. If no sort is supplied,
     * then defaults to sorting by {@code date} descending.
     * 
     * @param accountId Optional account ID to filter by; may be {@code null}
     * @param category Optional category to filter by; may be {@code null}
     * @param year Optional year to filter by; may be {@code null}
     * @param month Optional month to filter by; may be {@code null}
     * @param pageable Pagination information
     * @return A {@code Page} of transactions matching the provided filters
     * @throws AccountNotFoundException if the provided accountId does not exist
     */
    Page<Transaction> search(Long accountId, CategoryEnum category, Integer year, Integer month, Pageable pageable);


    /**
     * Goes through the ledger to identify a list of {@code Transactions.description(s)} that
     * are close matches or contains the query name. 
     * 
     * <ul>
     *  <li>We do a prefix match first and fill up the remaining results with a contains match for convenience.</li>
     *  <li>Only searches the ledger for the current user (TO BE IMPLEMENTED WHEN AUTH).</li>
     * </ul>
     * 
     * @param name the query name we suggest for
     * @param howMany how many values to return
     * @return a list of {@link String} that are valid suggestions to the user
     * @throws
     */
    List<String> suggest(String name, int howMany);

    /**
     * Retrieves autofill details for a previously used transaction description.
     *
     * <p>This is used by the UI to auto-populate other transaction fields (e.g. category, income/expense,
     * and account) after the user selects a description suggestion.</p>
     *
     * <p>The returned {@link TxSuggestionDetails} is typically derived from the most recent transaction
     * matching the given description (case-insensitive).</p>
     *
     * @param description the transaction description to look up (will be trimmed/validated)
     * @return a {@link TxSuggestionDetails} record containing autofill defaults for the given description
     * @throws BadRequestException if {@code description} is null/blank or too short to be valid
     * @throws TransactionNotFoundException if no matching transaction exists for the description
     */
    TxSuggestionDetails suggestionDetails(String description);

    /**
     * Finds a {@link Transaction} by its unique ID.
     * 
     * @param id the ID of the transaction to find
     * @return the found transaction
     * @throws TransactionNotFoundException if no transaction with the given ID exists
     * @throws BadRequestException if the provided ID is not positive or is {@code null}
     */
    Transaction findTransaction(Long id);

    /**
     * Updates an existing {@link Transaction} by replacing all fields with those
     * from the provided {@code transaction}.
     * 
     * <ul>
     * <li>All fields in {@code transaction} are used to update the existing
     * record.</li>
     * <li>The {@code id} field in {@code transaction} is ignored; the provided
     * {@code id} parameter is used to locate the existing record.</li>
     * <li>If no existing record is found with the given {@code id}, a
     * {@link TransactionNotFoundException} is thrown.</li>
     * </ul>
     * 
     * @param id the ID of the transaction to update
     * @param transaction the transaction data to update with (all fields used)
     * @return the updated transaction
     * @throws TransactionNotFoundException if no transaction with the given ID exists
     * @throws BadRequestException if input is {@code null} or fails validation
     */
    Transaction updateTransactionComplete(Long id, Transaction transaction);

    /**
     * Partially updates an existing {@link Transaction} with the non-null fields
     * from the provided {@code transaction}.
     * 
     * <ul>
     *  <li>Only non-null fields in {@code transaction} are used to update the
     *  existing record.</li>
     *  <li>The {@code id} field in {@code transaction} is ignored; the provided
     *  {@code id} parameter is used to locate the existing record.</li>
     *  <li>If no existing record is found with the given {@code id}, a
     *  {@link TransactionNotFoundException} is thrown.</li>
     * </ul>
     * @param id the ID of the transaction to update
     * @param transaction the transaction data to update with (only non-null fields used)
     * @return the updated transaction
     * @throws TransactionNotFoundException if no transaction with the given ID exists
     * @throws BadRequestException if input fails validation
     */
    Transaction updateTransaction(Long id, Transaction transaction);

    /**
     * Deletes the {@link Transaction} with the given ID.
     * 
     * @param id the ID of the transaction to delete
     * @throws TransactionNotFoundException if no transaction with the given ID exists
     * @throws BadRequestException if the provided ID is not positive or is {@code null}
     */
    void delete(Long id);

    /**
     * Check if a {@link Transaction} exists by its ID.
     * 
     * 
     * @param id the ID of the transaction to check
     * @return {@code true} if a transaction with the given ID exists, {@code false} otherwise. Invalid IDs return false.
     * 
     */
    boolean isExists(Long id);

    /**
     * Calculates the total balance from all transactions in the ledger.
     * 
     * @return the total balance as a {@link Money} object
     */
    Money calculateTotalBalance();

    /**
     * Finds and returns a {@code Page} of all transactions that occurred in the specified month.
     * 
     * @param year the year to filter transactions by; must not be {@code null}
     * @param month the month to filter transactions by; must not be {@code null}
     * @param pageable pagination information
     * @return a page of transactions that occurred in the specified month
     * @throws BadRequestException if the provided values of year or month are {@code null} or invalid
     */
    Page<Transaction> findTransactionsByMonth(Integer year, Integer month, Pageable pageable);

    /**
     * Calculates the net balance for the specified month.
     * 
     * @param year the year to calculate the balance for
     * @param month the month to calculate the balance for
     * @return the net balance as a {@link Money} object
     * @throws BadRequestException if the provided year or month is {@code null} or invalid
     */
    Money calculateMonthlyBalance(Integer year, Integer month);

    /**
     * Finds and returns a {@code Page} of all transactions associated with the specified category.
     * 
     * @param category the category to filter transactions by
     * @param pageable pagination information
     * @return a page of transactions in the specified category
     * @throws BadRequestException if the provided category is {@code null}
     */
    Page<Transaction> findTransactionsByCategory(CategoryEnum category, Pageable pageable);

    /**
     * Calculates the total amount for transactions in the specified category.
     * 
     * @param category the category to calculate the total for
     * @return the total amount as a {@link Money} object
     * @throws BadRequestException if the provided category is {@code null}
     */
    Money calculateTotalByCategory(CategoryEnum category);


    /**
     * Calculates the total amount for transactions in the specified account.
     * 
     * @param account the account to calculate for
     * @return the total amount as {@link Money} object
     * @throws BadRequestException if the provided account is null or not valid
     * @throws AccountNotFoundException if the provided account does not exist in the repository
     */
    Money calculateTotalByAccount(Account account);

    /**
     * Returns a monthly summary of transaction totals grouped by category.
     * <p>
     * The summary covers the full calendar month specified by {@code year} and {@code month}
     * (from the first day to the last day, inclusive). Categories with no transactions in the
     * month are still included with a total of {@code 0}.
     *
     * @param year the calendar year (e.g., 2026)
     * @param month the calendar month (1-12)
     * @return a list of category summaries for the month (one entry per {@link CategoryEnum})
     * @throws BadRequestException if {@code month} is not in the range 1-12
     */
    List<CategorySummaryDto> monthlySummaryByCategory(int year, int month);

    /**
     * Calculates the total income from the ledger between the a given date range.
     * Accpets parameters of type {@link LocalDate} to maintain accurary and convenience.
     * 
     * @param start the starting date as a {@link LocalDate}
     * @param end the ending date as a {@link LocalDate}
     * @return the income amount as {@link Money}
     * @throws BadRequestException if any of the provided parameters are {@code null}
     */
    Money calculateIncomeBetween(LocalDate start, LocalDate end);

    /**
     * Calculates the total expenses from the ledger between the a given date range.
     * Accpets parameters of type {@link LocalDate} to maintain accurary and convenience.
     * 
     * @param start the starting date as a {@link LocalDate}
     * @param end the ending date as a {@link LocalDate}
     * @return the total expense as {@link Money}
     * @throws BadRequestException if any of the provided parameters are {@code null}
     */
    Money calculateExpensesBetween(LocalDate start, LocalDate end);
}
