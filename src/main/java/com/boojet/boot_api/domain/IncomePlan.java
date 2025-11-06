package com.boojet.boot_api.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "income_plans")
public class IncomePlan {
    
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore // client does not need user, avoid lazy-proxy serialization issues
    private User user;

    @JsonProperty("source")
    private String sourceName;

    @Enumerated(EnumType.STRING)                // Store enum as string in DB for readability
    private PayType payType;

    @Column(precision = 19, scale = 2)          // Define precision and scale for Money
    private Money amount;                       // Represnts the payment amount, money received per hour/check/anually/etc

    @Column(precision = 9, scale = 2)
    private BigDecimal hoursPerWeek;            // Relevant for HOURLY pay type

    @Builder.Default
    private LocalDate effectiveFrom = LocalDate.now(); // Default to current date

    private LocalDate effectiveTo;                 // Null means ongoing

    // --- Domain constants ---
    private static final BigDecimal WEEKS_PER_YEAR = new BigDecimal("52");
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");
    private static final BigDecimal WEEKS_PER_MONTH = WEEKS_PER_YEAR.divide(MONTHS_PER_YEAR, 10, RoundingMode.HALF_UP);



    public boolean activeIn(YearMonth ym){
        var start = ym.atDay(1);
        var end = ym.atEndOfMonth();

        if(effectiveFrom != null && effectiveFrom.isAfter(end)) return false;
        if(effectiveTo != null && effectiveTo.isBefore(start)) return false;
        return true;
    }

    public Money calculateMonthlyAmount(YearMonth ym){

        if(!activeIn(ym)) return Money.zero();

        if (payType == PayType.ANNUAL || payType == PayType.MONTHLY || payType == PayType.WEEKLY || payType == PayType.BIWEEKLY){
            //hoursPerWeek is not relevant for these pay types
            setHoursPerWeek(null);
        }


        switch(payType){
            case HOURLY -> {

                //require hoursPerWeek to have a value for HOURLY payType
                if(hoursPerWeek == null){
                    throw new IllegalStateException("hoursPerWeek is required for HOURLY payTyple");
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
                BigDecimal paychecksPerMonth = new BigDecimal("26").divide(MONTHS_PER_YEAR, 10, RoundingMode.HALF_UP);
                BigDecimal expectedMonthlyPay = amount.asBigDecimal().multiply(paychecksPerMonth);

                return Money.of(expectedMonthlyPay);
            }
            case MONTHLY -> {
                return amount;
            }
            case ANNUAL -> {
                BigDecimal expectedMonthlyPay = amount.asBigDecimal().divide(MONTHS_PER_YEAR, 10, RoundingMode.HALF_UP);

                return Money.of(expectedMonthlyPay);
            }
            default -> {
                return Money.zero();
            }
        }
    }
}
