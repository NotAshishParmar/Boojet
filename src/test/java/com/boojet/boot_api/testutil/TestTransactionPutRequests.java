package com.boojet.boot_api.testutil;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.dto.transaction.TransactionPutRequest;

//test builder class for TransactionCreateRequests
public final class TestTransactionPutRequests {

    private String description = "Dining with friends";
    private Money amount = Money.of("80.00");
    private LocalDate date = LocalDate.of(2026, 2, 2);
    private Long categoryId = TestCategories.diningOut().getId();
    private Long accountId = TestAccounts.savings().getId();
    private Long toAccountId = null;

    //private constructor so that this helper class can only be created using the static builder() method.
    private TestTransactionPutRequests(){
    }

    //entry point for creating a builder-style test request.
    public static TestTransactionPutRequests builder(){
        return new TestTransactionPutRequests();
    }

    //convenience method for the most common case
    public static TransactionPutRequest validExpense(){
        return builder().build();
    }

    public TestTransactionPutRequests withDescription(String description){
        this.description = description;
        return this;
    }

    public TestTransactionPutRequests withAmount(Money amount){
        this.amount = amount;
        return this;
    }

    public TestTransactionPutRequests withDate(LocalDate date){
        this.date = date;
        return this;
    }

    public TestTransactionPutRequests withCategoryId(Long categoryId){
        this.categoryId = categoryId;
        return this;
    }

    public TestTransactionPutRequests withAccountId(Long accountId){
        this.accountId = accountId;
        return this;
    }

    public TestTransactionPutRequests withToAccountId(Long toAccountId){
        this.toAccountId = toAccountId;
        return this;
    }


    public TransactionPutRequest build(){
        return new TransactionPutRequest(
            description,
            amount,
            date,
            categoryId,
            accountId,
            toAccountId
        );
    }
}