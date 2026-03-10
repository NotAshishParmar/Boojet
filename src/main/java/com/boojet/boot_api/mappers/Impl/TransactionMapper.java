package com.boojet.boot_api.mappers.Impl;


import org.springframework.stereotype.Component;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.dto.transaction.TransactionResponse;
import com.boojet.boot_api.mappers.Mapper;

@Component
public class TransactionMapper implements Mapper<Transaction, TransactionResponse> {

    @Override
    public TransactionResponse mapTo(Transaction tx) {

        Category cat = tx.getCategory();
        Account acc = tx.getAccount();
        Account to = tx.getToAccount();

        return new TransactionResponse(
                tx.getId(),
                tx.getDescription(),
                tx.getAmount(),
                tx.getDate(),

                cat != null ? cat.getId() : null,
                cat != null ? cat.getName() : null,
                cat != null ? cat.getCode() : null,
                cat != null ? cat.getType() : null,

                tx.isIncome(),

                acc != null ? acc.getId() : null,
                acc != null ? acc.getName() : null,
                acc != null ? acc.getType() : null,

                to != null ? to.getId() : null,
                to != null ? to.getName() : null,
                to != null ? to.getType() : null);
    }

    @Override
    public Transaction mapFrom(TransactionResponse ignored) {
        throw new UnsupportedOperationException(
                "Use TransactionCreateRequest, TransactionPutRequest, TransactionPatchRequest for writes.");
    }
}
