package com.boojet.boot_api.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

import jakarta.persistence.Column;

import com.boojet.boot_api.exceptions.BadRequestException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents a recurring income source used to project expected income for a given month.
 * <p>
 * An {@code IncomePlan} becomes "effective" during a date range defined by {@link #effectiveFrom}
 * (inclusive) and {@link #effectiveTo} (inclusive when present). If {@code effectiveTo} is {@code null},
 * the plan is considered ongoing.
 *
 * <p><b>Pay types:</b>
 * <ul>
 *   <li>{@link PayType#HOURLY}: requires {@link #hoursPerWeek}.</li>
 *   <li>{@link PayType#WEEKLY}, {@link PayType#BIWEEKLY}, {@link PayType#MONTHLY}, {@link PayType#ANNUAL}:
 *       {@code hoursPerWeek} is not used.</li>
 * </ul>
 *
 * <p><b>Projection rules:</b>
 * This entity provides {@link #calculateMonthlyAmount(YearMonth)} to estimate the expected income
 * contributed by this plan in a specific month.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "income_plans")
public class IncomePlan {
    
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore // client does not need user, avoid lazy-proxy serialization issues
    private User user;

    private String sourceName;

    @Enumerated(EnumType.STRING)                // Store enum as string in DB for readability
    private PayType payType;

    @Column(precision = 19, scale = 2)          // Define precision and scale for Money
    private Money amount;                       // Represnts the payment amount, money received per hour/check/anually/etc

    @Column(precision = 9, scale = 2)
    private BigDecimal hoursPerWeek;            // Relevant for HOURLY pay type

    @Builder.Default
    private LocalDate effectiveFrom = LocalDate.now(); // Default to current date

    private LocalDate effectiveTo;              // Null means ongoing

    // --- Domain constants ---
    private static final BigDecimal WEEKS_PER_YEAR = new BigDecimal("52");
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");
    private static final BigDecimal WEEKS_PER_MONTH = WEEKS_PER_YEAR.divide(MONTHS_PER_YEAR, 10, RoundingMode.HALF_UP);


    /**
     * Determines whether this income plan applies during the given month.
     * <p>
     * A plan is considered active for {@code ym} if its effective date range overlaps
     * with the calendar month (inclusive boundaries).
     *
     * @param ym the month to evaluate
     * @return {@code true} if the plan overlaps the month; otherwise {@code false}
     */
    public boolean activeIn(YearMonth ym){
        var start = ym.atDay(1);
        var end = ym.atEndOfMonth();

        if(effectiveFrom != null && effectiveFrom.isAfter(end)) return false;
        if(effectiveTo != null && effectiveTo.isBefore(start)) return false;
        return true;
    }


    /**
     * Calculates the expected income contributed by this plan in the given month.
     * <p>
     * If the plan is not active in the month (see {@link #activeIn(YearMonth)}), this returns {@link Money#zero()}.
     *
     * <p><b>Calculation rules:</b>
     * <ul>
     *   <li>HOURLY: {@code hoursPerWeek * weeksPerMonth * hourlyRate}</li>
     *   <li>WEEKLY: {@code weeklyPay * weeksPerMonth}</li>
     *   <li>BIWEEKLY: {@code paycheckAmount * (26 / 12)}</li>
     *   <li>MONTHLY: {@code monthlyPay}</li>
     *   <li>ANNUAL: {@code annualSalary / 12}</li>
     * </ul>
     *
     * @param ym the month to calculate for
     * @return expected income for the month
     * @throws BadRequestException if {@link #payType} is {@link PayType#HOURLY} and {@link #hoursPerWeek} is {@code null}
     */
    public Money calculateMonthlyAmount(YearMonth ym) {

    if (!activeIn(ym)) return Money.zero();

    if (payType == null) return Money.zero();
    if (amount == null) return Money.zero();

    switch (payType) {
        case HOURLY -> {
            if (hoursPerWeek == null) {
                throw new BadRequestException("hoursPerWeek is required for HOURLY payType");
            }

            BigDecimal hoursPerMonth = hoursPerWeek.multiply(WEEKS_PER_MONTH);
            BigDecimal monthlyPay = hoursPerMonth.multiply(amount.asBigDecimal());
            return Money.of(monthlyPay);
        }
        case WEEKLY -> {
            BigDecimal expectedMonthlyPay = amount.asBigDecimal().multiply(WEEKS_PER_MONTH);
            return Money.of(expectedMonthlyPay);
        }
        case BIWEEKLY -> {
            BigDecimal paychecksPerMonth =
                new BigDecimal("26").divide(MONTHS_PER_YEAR, 10, RoundingMode.HALF_UP);
            BigDecimal expectedMonthlyPay = amount.asBigDecimal().multiply(paychecksPerMonth);
            return Money.of(expectedMonthlyPay);
        }
        case MONTHLY -> {
            return amount;
        }
        case ANNUAL -> {
            BigDecimal expectedMonthlyPay =
                amount.asBigDecimal().divide(MONTHS_PER_YEAR, 10, RoundingMode.HALF_UP);
            return Money.of(expectedMonthlyPay);
        }
        default -> {
            return Money.zero();
        }
    }
}
}
