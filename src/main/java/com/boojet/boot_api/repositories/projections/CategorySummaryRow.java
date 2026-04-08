package com.boojet.boot_api.repositories.projections;

import java.math.BigDecimal;

public record CategorySummaryRow(
    Long categoryId,
    String categoryName,
    String categoryCode,
    BigDecimal total
) {}