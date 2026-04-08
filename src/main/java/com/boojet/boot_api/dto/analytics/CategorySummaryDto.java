package com.boojet.boot_api.dto.analytics;

import com.boojet.boot_api.domain.Money;

public record CategorySummaryDto(
    Long categoryId,
    String categoryName,
    String categoryCode,
    Money total
) {}
