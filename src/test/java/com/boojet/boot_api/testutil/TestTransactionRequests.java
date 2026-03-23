package com.boojet.boot_api.testutil;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.dto.transaction.TransactionCreateRequest;

public final class TestTransactionRequests {

    private TestTransactionRequests() {
    }

    public static TransactionCreateRequest validExpenseRequest() {
        return new TransactionCreateRequest(
                "Groceries",
                Money.of("45.99"),
                LocalDate.of(2026, 3, 22),
                10L,
                1L,
                null
        );
    }

    public static TransactionCreateRequest validIncomeRequest() {
        return new TransactionCreateRequest(
                "Salary",
                Money.of("2500.00"),
                LocalDate.of(2026, 3, 1),
                11L,
                1L,
                null
        );
    }

    public static TransactionCreateRequest validTransferRequest() {
        return new TransactionCreateRequest(
                "Move to savings",
                Money.of("300.00"),
                LocalDate.of(2026, 3, 22),
                12L,
                1L,
                2L
        );
    }
}