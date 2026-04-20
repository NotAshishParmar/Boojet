package com.boojet.boot_api.dto.incomePlan;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.PayType;
import com.fasterxml.jackson.databind.JsonNode;

public record IncomePlanPatchRequest(
    String sourceName,
    PayType payType,
    Money amount,
    JsonNode estimatedDeductionRate,
    JsonNode hoursPerWeek,
    LocalDate effectiveFrom,
    JsonNode effectiveTo
) {}
