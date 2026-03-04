package com.boojet.boot_api.services;

import java.time.LocalDate;
import java.util.List;

import com.boojet.boot_api.domain.AccountBalanceSnapshot;
import com.boojet.boot_api.domain.Money;

/**
 * Service interface for managing account balance snapshots.
 * 
 * This service provides operations for upserting account balance snapshots,
 * which represent the balance of an account at a specific point in time.
 */
public interface AccountBalanceSnapshotService {
    
    /**
     * Upserts an account balance snapshot.
     * 
     * Creates a new account balance snapshot or updates an existing one if a snapshot
     * for the given account and date already exists.
     * 
     * @param accountId the unique identifier of the account. Must not be null.
     * @param asOfDate the date as of which the balance snapshot is recorded. Must not be null.
     * @param balance the balance amount for the account on the given date. Must not be null.
     * @return the created or updated {@link AccountBalanceSnapshot}
     */
    AccountBalanceSnapshot upsert(Long accountId, LocalDate asOfDate, Money balance);

    /**
     * Retrieves a list of all account balance snapshots for a specific account.
     * 
     * Fetches all recorded balance snapshots associated with the given account,
     * ordered by date (typically in ascending order).
     * 
     * @param accountId the unique identifier of the account. Must not be null.
     * @return a {@link List} of {@link AccountBalanceSnapshot} objects for the account.
     *         Returns an empty list if no snapshots are found for the given account.
     */
    List<AccountBalanceSnapshot> listSnapshots(Long accountId);
}
