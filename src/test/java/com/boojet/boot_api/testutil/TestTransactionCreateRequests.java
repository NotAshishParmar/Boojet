package com.boojet.boot_api.testutil;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.dto.transaction.TransactionCreateRequest;

//test builder class for TransactionCreateRequests
public final class TestTransactionCreateRequests {

    private String description = "Groceries";
    private Money amount = Money.of("45.99");
    private LocalDate date = LocalDate.of(2026, 1, 1);
    private Long categoryId = TestCategories.groceries().getId();
    private Long accountId = TestAccounts.chequing().getId();
    private Long toAccountId = null;

    //private constructor so that this helper class can only be created using the static builder() method.
    private TestTransactionCreateRequests(){
    }

    //entry point for creating a builder-style test request.
    public static TestTransactionCreateRequests builder(){
        return new TestTransactionCreateRequests();
    }

    //convenience method for the most common case
    public static TransactionCreateRequest validExpense(){
        return builder().build();
    }

    public TestTransactionCreateRequests withDescription(String description){
        this.description = description;
        return this;
    }

    public TestTransactionCreateRequests withAmount(Money amount){
        this.amount = amount;
        return this;
    }

    public TestTransactionCreateRequests withDate(LocalDate date){
        this.date = date;
        return this;
    }

    public TestTransactionCreateRequests withCategoryId(Long categoryId){
        this.categoryId = categoryId;
        return this;
    }

    public TestTransactionCreateRequests withAccountId(Long accountId){
        this.accountId = accountId;
        return this;
    }

    public TestTransactionCreateRequests withToAccountId(Long toAccountId){
        this.toAccountId = toAccountId;
        return this;
    }


    public TransactionCreateRequest build(){
        return new TransactionCreateRequest(
            description,
            amount,
            date,
            categoryId,
            accountId,
            toAccountId
        );
    }
}