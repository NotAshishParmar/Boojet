package com.boojet.boot_api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boojet.boot_api.dto.credit.CreditMonthlySummaryResponse;
import com.boojet.boot_api.services.InsightsService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/insights/credit")
public class CreditInsightsController {

    private final InsightsService insightsService;

    public CreditInsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @Operation(summary = "Credit monthly summary", description = "Credit accumulated (card spending), paid off (payments to cards), and net change for a month.")
    @GetMapping("/monthly")
    public CreditMonthlySummaryResponse monthly(@RequestParam int year, @RequestParam int month) {
        return insightsService.creditMonthly(year, month);
    }
}
