package com.boojet.boot_api.controllers;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boojet.boot_api.dto.analytics.EssentialVsNonEssentialResponse;
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
}
