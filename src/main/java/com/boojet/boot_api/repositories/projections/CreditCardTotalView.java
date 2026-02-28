package com.boojet.boot_api.repositories.projections;

import java.math.BigDecimal;

public interface CreditCardTotalView {
    Long getAccountId();
    String getAccountName();
    BigDecimal getTotal();
}
