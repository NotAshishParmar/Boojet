package com.boojet.boot_api.dto.transaction;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;

public record TransactionPutRequest(
    String description,
    Money amount,
    LocalDate date,
    Long categoryId,
    Long accountId,
    Long toAccountId
) {}
