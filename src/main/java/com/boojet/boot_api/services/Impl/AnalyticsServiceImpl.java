package com.boojet.boot_api.services.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.AccountBalanceSnapshot;
import com.boojet.boot_api.domain.AccountType;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.dto.analytics.CategorySummaryDto;
import com.boojet.boot_api.dto.analytics.EssentialVsNonEssentialResponse;
import com.boojet.boot_api.dto.analytics.MonthlyDebtResponse;
import com.boojet.boot_api.dto.analytics.MonthlyNetResponse;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.repositories.AccountBalanceSnapshotRepository;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.IncomePlanRepository;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.repositories.projections.CategorySummaryRow;
import com.boojet.boot_api.repositories.projections.CategoryTotalView;
import com.boojet.boot_api.repositories.projections.EssentialSpendingAggregate;
import com.boojet.boot_api.services.AnalyticsService;
import com.boojet.boot_api.services.IncomePlanService;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Set<AccountType> DEBT_ACCOUNT_TYPES = EnumSet.of(AccountType.CREDIT_CARD,
            AccountType.MASTER_CARD, AccountType.LOAN);

    private final TransactionRepository transactionRepo;
    private final AccountRepository accountRepo;
    private final AccountBalanceSnapshotRepository accountBalanceSnapshotRepo;
    private final IncomePlanService incomePlanService;

    public AnalyticsServiceImpl(TransactionRepository transactionRepo, AccountRepository accountRepo,
            AccountBalanceSnapshotRepository accountBalanceSnapshotRepo, IncomePlanService incomePlanService) {
        this.transactionRepo = transactionRepo;
        this.accountRepo = accountRepo;
        this.accountBalanceSnapshotRepo = accountBalanceSnapshotRepo;
        this.incomePlanService = incomePlanService;
    }

    @Override
    public EssentialVsNonEssentialResponse essentialVsNonEssential(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<EssentialSpendingAggregate> aggregates = transactionRepo.aggregateExpenseByEssential(fromDate, toDate);

        EssentialSpendingAggregate essentialAggregate = aggregates.stream()
                .filter(a -> Boolean.TRUE.equals(a.essential()))
                .findFirst()
                .orElse(new EssentialSpendingAggregate(true, BigDecimal.ZERO, 0L));

        EssentialSpendingAggregate nonEssentialAggregate = aggregates.stream()
                .filter(a -> Boolean.FALSE.equals(a.essential()))
                .findFirst()
                .orElse(new EssentialSpendingAggregate(false, BigDecimal.ZERO, 0L));

        BigDecimal essentialAmount = essentialAggregate.totalAmount();
        BigDecimal nonEssentialAmount = nonEssentialAggregate.totalAmount();

        long essentialCount = essentialAggregate.transactionCount();
        long nonEssentialCount = nonEssentialAggregate.transactionCount();

        BigDecimal totalAmount = essentialAmount.add(nonEssentialAmount);
        long totalCount = essentialCount + nonEssentialCount;
        Math.toIntExact(essentialCount);

        BigDecimal essentialPercentage = percentageOf(essentialAmount, totalAmount);
        BigDecimal nonEssentialPercentage = percentageOf(nonEssentialAmount, totalAmount);

        return new EssentialVsNonEssentialResponse(
                fromDate,
                toDate,
                Money.of(totalAmount),
                Math.toIntExact(totalCount),
                new EssentialVsNonEssentialResponse.SpendingBucketDto(
                        Money.of(essentialAmount),
                        Math.toIntExact(essentialCount),
                        essentialPercentage),
                new EssentialVsNonEssentialResponse.SpendingBucketDto(
                        Money.of(nonEssentialAmount),
                        Math.toIntExact(nonEssentialCount),
                        nonEssentialPercentage));

    }

    @Override
    public List<CategorySummaryDto> monthlySummaryBySubCategory(int year, int month) {
        YearMonth ym = buildYearMonthOrThrow(year, month);

        List<CategorySummaryRow> rows = transactionRepo.sumNetBySubCategoryBetween(ym.atDay(1), ym.atEndOfMonth());

        return rows.stream()
                .map(r -> new CategorySummaryDto(
                        r.categoryId(),
                        r.categoryName(),
                        r.categoryCode(),
                        Money.of(r.total())))
                .toList();
    }

    @Override
    public List<CategorySummaryDto> monthlySummaryByParentCategory(int year, int month) {
        YearMonth ym = buildYearMonthOrThrow(year, month);

        List<CategorySummaryRow> rows = transactionRepo.sumNetByParentCategoryBetween(ym.atDay(1), ym.atEndOfMonth());

        return rows.stream()
                .map(r -> new CategorySummaryDto(
                        r.categoryId(),
                        r.categoryName(),
                        r.categoryCode(),
                        Money.of(r.total())))
                .toList();
    }

    @Override
    public List<MonthlyDebtResponse> monthlyDebtTrend(YearMonth fromMonth, YearMonth toMonth) {

        validateDateRange(fromMonth, toMonth);

        List<Account> debtAccounts = accountRepo.findByTypeIn(DEBT_ACCOUNT_TYPES);

        List<MonthlyDebtResponse> response = new ArrayList<>();
        YearMonth current = fromMonth;

        // for each month starting at fromMonth, calculate the total debt at month end
        // for each debt Account and add it the the response list
        while (!current.isAfter(toMonth)) {
            final YearMonth month = current;

            Money totalDebtAtMonthEnd = debtAccounts.stream()
                    .map(account -> calculateDebtForAccountAtMonthEnd(account, month))
                    .reduce(Money.zero(), Money::add);

            response.add(new MonthlyDebtResponse(month, totalDebtAtMonthEnd));
            current = current.plusMonths(1);
        }

        return response;
    }

    // ---------------------------------------------BUSINESS LOGIC
    // HELPERS-----------------------------------------------------

    // to estimate account balance at month end, get the most recent account balance
    // snapshot for that account for that month
    // and add to it the transactions on that account between balance snapshot date
    // and month end
    private Money calculateDebtForAccountAtMonthEnd(Account account, YearMonth month) {

        Money balance = Money.zero();
        LocalDate startDate = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        Optional<AccountBalanceSnapshot> snapshot = accountBalanceSnapshotRepo
                .findTopByAccount_IdAndAsOfDateLessThanEqualOrderByAsOfDateDesc(account.getId(), monthEnd);

        // if snapshot exists then move up the start date and update balance to match
        // snapshot
        if (snapshot.isPresent()) {
            balance = snapshot.get().getBalanceAmount();
            startDate = snapshot.get().getAsOfDate().plusDays(1); // possible site for ERROR???? what if asOfDate is for
                                                                  // the last day of month
        }

        // get a net of transactions between snapshot start and month end
        BigDecimal balanceDiff = transactionRepo.sumNetForAccountBetween(account.getId(), startDate, monthEnd);

        Money balanceAfterSnap = Money.of(balanceDiff);

        balance = balance.add(balanceAfterSnap);

        // negation since total debt is represented as positive but debt on balance
        // account is negative
        Money debt = balance.negate();

        // negative debt implies wealth therefore no debt
        if (debt.isNegative())
            return Money.zero();

        return debt;
    }

    public MonthlyNetResponse monthlyNetReport(int year, int month) {
        Money expenses = incomePlanService.getActualMonthlyExpenses(year, month);
        Money expectedGrossIncome = incomePlanService.getGrossExpectedMonthlyIncome(year, month);
        Money expectedNetIncome = incomePlanService.getNetExpectedMonthlyIncome(year, month);
        Money actualIncome = incomePlanService.getActualMonthlyIncome(year, month);

        YearMonth ym = buildYearMonthOrThrow(year, month);

        return new MonthlyNetResponse(ym.toString(), expectedGrossIncome, expectedNetIncome, actualIncome, expenses,
                expectedNetIncome.subtract(expenses), actualIncome.subtract(expenses));
    }

    // ----------------------------------------------HELPERS--------------------------------------------------

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null)
            throw new BadRequestException("Start date cannot be null");
        if (to == null)
            throw new BadRequestException("End date cannot be null");

        if (from.isAfter(to))
            throw new BadRequestException("Start date cannot be after End date");
    }

    private void validateDateRange(YearMonth from, YearMonth to) {
        if (from == null)
            throw new BadRequestException("Starting YearMonth value cannot be null");
        if (to == null)
            throw new BadRequestException("Ending YearMonth value cannot be null");

        if (from.isAfter(to))
            throw new BadRequestException("Starting YearMonth cannot be after Ending YearMonth");
    }

    private YearMonth buildYearMonthOrThrow(int year, int month) {
        try {
            return YearMonth.of(year, month);
        } catch (RuntimeException e) {
            throw new BadRequestException("Cannot build YearMonth. Invalid Year/Month.");
        }
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return amount
                .multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

}
