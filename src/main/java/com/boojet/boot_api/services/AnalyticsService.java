package com.boojet.boot_api.services;

import java.time.LocalDate;

import com.boojet.boot_api.dto.analytics.EssentialVsNonEssentialResponse;

public interface AnalyticsService {
    EssentialVsNonEssentialResponse essentialVsNonEssential(LocalDate fromDate, LocalDate toDate);
}
