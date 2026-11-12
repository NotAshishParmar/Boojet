package com.boojet.boot_api.dto.account;

import java.time.LocalDate;

import com.boojet.boot_api.domain.AccountType;
import com.boojet.boot_api.domain.Money;

public record AccountResponse (
    Long id,
    String name,
    AccountType type,
    Money openingBalance,
    LocalDate createdAt,
    LocalDate closedAt
) {}
