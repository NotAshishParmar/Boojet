package com.boojet.boot_api.controllers.dto;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;

public record TransactionCreateRequest (
    String description,
    Money amount,
    LocalDate date,
    Long categoryId,
    Long accountId,
    Long toAccountId
){}