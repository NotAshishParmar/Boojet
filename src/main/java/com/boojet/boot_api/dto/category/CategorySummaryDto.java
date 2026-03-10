package com.boojet.boot_api.dto.category;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;

public record CategorySummaryDto (
    Category category,
    Money total
){}
