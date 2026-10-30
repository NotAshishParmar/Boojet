package com.boojet.boot_api.controllers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boojet.boot_api.dto.analytics.CategorySummaryDto;
import com.boojet.boot_api.dto.analytics.EssentialVsNonEssentialResponse;
import com.boojet.boot_api.dto.analytics.MonthlyDebtResponse;
import com.boojet.boot_api.dto.analytics.MonthlyNetResponse;
import com.boojet.boot_api.services.AnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Analytics")
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Essential vs Non-Essential Transaction Summary", description = "Get a summary of essential vs non-essentail transactions for the requested period")
    @GetMapping("/essential-breakdown")
    public EssentialVsNonEssentialResponse essentialVsNonEssential(@RequestParam("fromDate") LocalDate fromDate, @RequestParam("toDate") LocalDate toDate){
        return analyticsService.essentialVsNonEssential(fromDate, toDate);
    }

    @Operation(summary = "Get monthly summary by sub-category", description = "Retrieve a summary of transactions for a specific month, grouped by sub-category.")
    @GetMapping("/monthly-summary-all/{year}/{month}")
    public List<CategorySummaryDto> monthlySummaryBySubCategory(@PathVariable int year, @PathVariable int month){
        return analyticsService.monthlySummaryBySubCategory(year, month);
    }

    @Operation(summary = "Get monthly summary by parent category", description = "Retrieve a summary of transactions for a specific month, grouped by parent category.")
    @GetMapping("/monthly-summary-parent/{year}/{month}")
    public List<CategorySummaryDto> monthlySummaryByParentCategory(@PathVariable int year, @PathVariable int month){
        return analyticsService.monthlySummaryByParentCategory(year, month);
    }

    @Operation(summary = "Get debt over time", description = "Retrieve a summary of debt at month end over the requested period of time")
    @GetMapping("/debt/monthly")
    public List<MonthlyDebtResponse> monthlyDebtTrend(@RequestParam YearMonth fromMonth, @RequestParam YearMonth toMonth){
        return analyticsService.monthlyDebtTrend(fromMonth, toMonth);
    }

    //net report
    @Operation(summary = "Get net report for a month", description = "Generate a net report for a specified month and year, detailing expectesd vs actual income and expenses. Also includes net expected and actual gain or loss calculations.")
    @GetMapping("/net/{year}/{month}")
    public MonthlyNetResponse net(@PathVariable int year, @PathVariable int month){
        return analyticsService.monthlyNetReport(year, month);
    }


}
