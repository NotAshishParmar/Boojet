package com.boojet.boot_api.services;

import java.time.LocalDate;
import java.util.Optional;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.exceptions.BadRequestException;

/**
 * Service interface for retrieving account balance information.
 * 
 * <p>
 * This service provides methods to query the balance of an account at a
 * specific point in time.
 * Implementations of this interface are responsible for calculating or
 * retrieving historical balance
 * data for accounts.
 * </p>
 */

public interface BalanceService {

    /**
     * Retrieves the balance of an account as of a specified date.
     * 
     * <p>
     * Returns the account balance at the start of the given target date. If no
     * balance information
     * is available for the specified account and date, an empty {@code Optional} is
     * returned.
     * </p>
     * 
     * @param accountId  the unique identifier of the account. Must not be null.
     * @param targetDate the date as of which to retrieve the balance. Must not be
     *                   null.
     * @return an {@code Optional} containing the account balance as of the target
     *         date, or an empty
     *         {@code Optional} if the balance cannot be determined for the given
     *         account and date.
     * @throws BadRequestException if {@code accountId} or {@code targetDate} is
     *                             null.
     */
    public Optional<Money> getBalanceAsOf(Long accountId, LocalDate targetDate);

    /**
     * Retrieves the current balance of an account.
     * 
     * <p>
     * Returns the most recent balance information for the specified account. If no
     * balance information
     * is available for the given account, an empty {@code Optional} is returned.
     * </p>
     * 
     * @param accountId the unique identifier of the account. Must not be null.
     * @return an {@code Optional} containing the current account balance, or an
     *         empty
     *         {@code Optional} if the balance cannot be determined for the given
     *         account.
     * @throws BadRequestException if {@code accountId} is null.
     */
    Optional<Money> getCurrentBalance(Long accountId);

}
