package com.boojet.boot_api.dto.transaction;

import java.time.LocalDate;

import com.boojet.boot_api.domain.AccountType;
import com.boojet.boot_api.domain.CategoryType;
import com.boojet.boot_api.domain.Money;

public record TransactionResponse (
    Long id,
    String description,
    Money amount,
    LocalDate date,

    Long categoryId,
    String categoryName,
    String categoryCode,
    CategoryType categoryType,

    boolean income,

    Long accountId,
    String accountName,
    AccountType accountType,

    Long toAccountId,
    String toAccountName,
    AccountType toAccountType
){}
