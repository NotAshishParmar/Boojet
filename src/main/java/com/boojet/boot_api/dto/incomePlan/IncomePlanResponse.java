package com.boojet.boot_api.dto.incomePlan;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.PayType;

public record IncomePlanResponse(
    Long id,
    String sourceName,
    PayType payType,
    Money amount,
    BigDecimal hoursPerWeek,
    LocalDate effectiveFrom,
    LocalDate effectiveTo
) {}
