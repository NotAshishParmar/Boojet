package com.boojet.boot_api.repositories.projections;

import java.math.BigDecimal;

public record EssentialSpendingAggregate (
    Boolean essential,
    BigDecimal totalAmount,
    Long transactionCount
){}
