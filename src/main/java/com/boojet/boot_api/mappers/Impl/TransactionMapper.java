package com.boojet.boot_api.mappers.Impl;

import org.springframework.stereotype.Component;

import com.boojet.boot_api.controllers.dto.TransactionResponse;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.mappers.Mapper;


@Component
public class TransactionMapper implements Mapper<Transaction, TransactionResponse> {

    @Override
    public TransactionResponse mapTo(Transaction tx) {
        
        Long categoryId = (tx.getCategory() != null) ? tx.getCategory().getId() : null;
        
        Long accountId = (tx.getAccount() != null) ? tx.getAccount().getId() : null;
        Long toAccountId = (tx.getToAccount() != null) ? tx.getToAccount().getId() : null;
        
        return new TransactionResponse(
                    tx.getId(),
                    tx.getDescription(),
                    tx.getAmount(),
                    tx.getDate(),
                    categoryId,
                    tx.isIncome(),
                    accountId,
                    toAccountId
        );
    }

    @Override
    public Transaction mapFrom(TransactionResponse ignored) {
        throw new UnsupportedOperationException("Use TransactionCreateRequest, TransactionPutRequest, TransactionPatchRequest for writes.");
    }
}




