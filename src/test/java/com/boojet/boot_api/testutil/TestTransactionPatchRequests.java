package com.boojet.boot_api.testutil;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.dto.transaction.TransactionPatchRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;

public class TestTransactionPatchRequests {

    private String description = "Dining with friends";
    private Money amount = Money.of("80.00");
    private LocalDate date = LocalDate.of(2026, 2, 2);
    private Long categoryId = TestCategories.diningOut().getId();
    private Long accountId = TestAccounts.savings().getId();
    private JsonNode toAccountId = null;

    // private constructor so this helper can only be created through builder()
    private TestTransactionPatchRequests() {
    }

    // entry point for creating a builder-style test request
    public static TestTransactionPatchRequests builder() {
        return new TestTransactionPatchRequests();
    }

    // convenience method for the most common case
    public static TransactionPatchRequest validExpense() {
        return builder().build();
    }

    public TestTransactionPatchRequests withDescription(String description) {
        this.description = description;
        return this;
    }

    public TestTransactionPatchRequests withAmount(Money amount) {
        this.amount = amount;
        return this;
    }

    public TestTransactionPatchRequests withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public TestTransactionPatchRequests withCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public TestTransactionPatchRequests withAccountId(Long accountId) {
        this.accountId = accountId;
        return this;
    }

    // use this when the patch should set/update toAccountId to a numeric value
    public TestTransactionPatchRequests withToAccountId(Long toAccountId) {
        this.toAccountId = LongNode.valueOf(toAccountId);
        return this;
    }

    // use this when the patch should explicitly clear toAccountId
    public TestTransactionPatchRequests withNullToAccountId() {
        this.toAccountId = NullNode.instance;
        return this;
    }

    // use this when the patch should omit toAccountId entirely
    public TestTransactionPatchRequests withoutToAccountId() {
        this.toAccountId = null;
        return this;
    }

    public TransactionPatchRequest build() {
        return new TransactionPatchRequest(
                description,
                amount,
                date,
                categoryId,
                accountId,
                toAccountId
        );
    }
}