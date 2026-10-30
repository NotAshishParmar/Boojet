package com.boojet.boot_api.dto.analytics;

import com.boojet.boot_api.domain.Money;

/**
 * Summary report for a given month.
 *
 * @param month          human-readable label for the month (e.g., "2026-01" or
 *                       "Jan 2026")
 * @param expectedIncome total projected income from income plans
 * @param actualIncome   total recorded income from transactions
 * @param expenses       total recorded expenses from transactions
 * @param netExpected    expectedIncome minus expenses
 * @param netActual      actualIncome minus expenses
 */
public record MonthlyNetResponse(
        String month,
        Money expectedGrossIncome,
        Money expectedNetIncome,
        Money actualIncome,
        Money expenses,
        Money netExpected,
        Money netActual) {
}
