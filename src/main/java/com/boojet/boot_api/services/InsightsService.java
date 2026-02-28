package com.boojet.boot_api.services;

import com.boojet.boot_api.controllers.dto.CreditMonthlySummaryResponse;

public interface InsightsService {
    CreditMonthlySummaryResponse creditMonthly(int year, int month);
}
