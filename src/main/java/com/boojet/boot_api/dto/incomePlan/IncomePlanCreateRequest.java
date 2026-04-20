package com.boojet.boot_api.dto.incomePlan;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.PayType;

public record IncomePlanCreateRequest(
    String sourceName,
    PayType payType,
    Money amount,
    BigDecimal estimatedDeductionRate,
    BigDecimal hoursPerWeek,
    LocalDate effectiveFrom,
    LocalDate effectiveTo
) {}
