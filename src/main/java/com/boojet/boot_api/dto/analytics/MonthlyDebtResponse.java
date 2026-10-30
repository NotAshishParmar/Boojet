package com.boojet.boot_api.dto.analytics;

import java.time.YearMonth;

import com.boojet.boot_api.domain.Money;

public record MonthlyDebtResponse(
    YearMonth ym,
    Money totalDebtAtMonthEnd    
) {}
