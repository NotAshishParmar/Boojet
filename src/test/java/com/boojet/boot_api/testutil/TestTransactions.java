package com.boojet.boot_api.testutil;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;

public class TestTransactions {
    
    private TestTransactions() {
    }

    public static Transaction groceriesExpense() {
        return Transaction.builder()
                .id(100L)
                .description("Groceries")
                .amount(Money.of("45.99"))
                .date(LocalDate.of(2026, 3, 22))
                .category(TestCategories.groceries())
                .income(false)
                .account(TestAccounts.chequing())
                .toAccount(null)
                .build();
    }

    public static Transaction salaryIncome() {
        return Transaction.builder()
                .id(101L)
                .description("Monthly salary")
                .amount(Money.of("2500.00"))
                .date(LocalDate.of(2026, 3, 1))
                .category(TestCategories.salary())
                .income(true)
                .account(TestAccounts.chequing())
                .toAccount(null)
                .build();
    }

    public static Transaction transferToSavings() {
        return Transaction.builder()
                .id(102L)
                .description("Move to savings")
                .amount(Money.of("300.00"))
                .date(LocalDate.of(2026, 3, 22))
                .category(TestCategories.transfer())
                .income(false)
                .account(TestAccounts.chequing())
                .toAccount(TestAccounts.savings())
                .build();
    }
}
