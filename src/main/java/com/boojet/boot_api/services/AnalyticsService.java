package com.boojet.boot_api.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.boojet.boot_api.dto.analytics.CategorySummaryDto;
import com.boojet.boot_api.dto.analytics.EssentialVsNonEssentialResponse;
import com.boojet.boot_api.dto.analytics.MonthlyDebtResponse;

public interface AnalyticsService {
    EssentialVsNonEssentialResponse essentialVsNonEssential(LocalDate fromDate, LocalDate toDate);

    /**
     * Returns a monthly summary of transaction totals grouped by category.
     * <p>
     * The summary covers the full calendar month specified by {@code year} and {@code month}
     * (from the first day to the last day, inclusive). Categories with no transactions in the
     * month are still included with a total of {@code 0}.
     *
     * @param year the calendar year (e.g., 2026)
     * @param month the calendar month (1-12)
     * @return a list of category summaries for the month (one entry per {@link Category})
     * @throws BadRequestException if {@code month} is not in the range 1-12
     */
    List<CategorySummaryDto> monthlySummaryBySubCategory(int year, int month);

    List<CategorySummaryDto> monthlySummaryByParentCategory(int year, int month);

    List<MonthlyDebtResponse> monthlyDebtTrend(YearMonth fromMonth, YearMonth toMonth);

}
