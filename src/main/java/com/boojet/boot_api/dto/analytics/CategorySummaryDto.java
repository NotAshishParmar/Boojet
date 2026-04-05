package com.boojet.boot_api.dto.analytics;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;

public record CategorySummaryDto (
    Category category,
    Money total
){}
