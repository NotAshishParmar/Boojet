package com.boojet.boot_api.dto.account;

import java.math.BigDecimal;

import com.boojet.boot_api.domain.Money;

public record LoanCreateRequest (
    Money originalPrincipal,
    BigDecimal annualInterestRate,
    Integer termMonths
) {}