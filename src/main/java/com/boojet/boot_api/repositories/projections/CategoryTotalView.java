package com.boojet.boot_api.repositories.projections;

import java.math.BigDecimal;

import com.boojet.boot_api.domain.Category;

public interface CategoryTotalView {
    Category getCategory();
    BigDecimal getTotal();
}
