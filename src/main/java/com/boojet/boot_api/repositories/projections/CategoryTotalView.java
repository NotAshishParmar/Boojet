package com.boojet.boot_api.repositories.projections;

import java.math.BigDecimal;

import com.boojet.boot_api.domain.CategoryEnum;

public interface CategoryTotalView {
    CategoryEnum getCategory();
    BigDecimal getTotal();
}
