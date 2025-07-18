package com.boojet;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionManagerTest {
    
    //helper to build a manager detached from disk
    private TransactionManager newManger(){
        return new TransactionManager(SaveMode.NONE);
    }

    @Test
    void balanceReflectsIncomeandExpense(){
        TransactionManager tm = newManger();

        tm.addTransaction(new Transaction("Salary", 
                                            new BigDecimal("1000.00"), 
                                            LocalDate.of(2025,7,1),
                                            Category.OTHER, 
                                            true));
        
        tm.addTransaction(new Transaction("Rent",
                                            new BigDecimal("400.00"),
                                            LocalDate.of(2025, 7,2),
                                            Category.RENT, 
                                            false));
                                            
        assertEquals(new BigDecimal("600.00"), tm.getBalance(), "Balance should be +600.00");
    }
}
