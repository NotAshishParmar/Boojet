package com.boojet.boot_api.controllers.dto;

import java.util.List;

import com.boojet.boot_api.domain.Money;

public record CreditMonthlySummaryResponse(
    int year,
    int month,
    Money accumulated,
    Money paidOff,
    Money netChange,
    List<CardBreakdown> byCard
) {
    public record CardBreakdown(
        Long accountId,
        String accountName,
        Money accumulated,
        Money paidOff,
        Money netChange
    ){}
}
