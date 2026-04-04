package com.boojet.boot_api.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;

public record EssentialVsNonEssentialResponse(
    LocalDate fromDate,
    LocalDate toDate,
    Money totalExpenseAmount,
    int totalExpenseTransactionCount,
    SpendingBucketDto essential,
    SpendingBucketDto nonEssential
) {
    public record SpendingBucketDto(
        Money amount,
        int transactionCount,
        BigDecimal percentageOfTotal
    ){}
}
