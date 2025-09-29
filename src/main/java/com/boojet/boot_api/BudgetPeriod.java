package com.boojet.boot_api;

import java.math.BigDecimal;
import java.time.YearMonth;

public class BudgetPeriod {
    
    private YearMonth id;           //monthly identifier for the period
    private BigDecimal expectedIncome;  //expected income based on past trends

    public BudgetPeriod (YearMonth period){
        this.id = period;
    }

    public void setExpectedIncome(BigDecimal expectedIncome){
        this.expectedIncome = expectedIncome;
    }

}
