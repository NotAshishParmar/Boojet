package com.boojet.boot_api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionManagerTest {

    private TransactionManager manager;
    
    private final Transaction salary = tx("Salary1", "1000.00", "2025-07-01", Category.INCOME, true);
    private final Transaction salary2 = tx("Salary2", "2000.00", "2025-07-02", Category.INCOME, true);
    private final Transaction rent = tx("Rent", "400.00", "2025-07-03", Category.OTHER, false);
    private final Transaction food = tx("Pizza", "1000.00", "2025-08-04", Category.FOOD, false);
    private final Transaction food2 = tx("Hotdog", "2000.00", "2025-08-05", Category.FOOD, false);


    @BeforeEach
    void setup(){
        manager = new TransactionManager(SaveMode.NONE);
    }

    //----------addTransaction---------------
    @Test
    void addTransactionAndCheckDirty(){

        manager.addTransaction(salary);
        assertTrue(manager.hasUnsavedChanges(), "Manager should be dirty after adding transaction");
    }

    //----------updateTransaction---------------
    @Test
    void shouldReplaceCorrectly(){
        manager.addTransaction(salary);
        manager.addTransaction(salary2);

        manager.updateTransaction(1, rent);

        List<Transaction> list = manager.getTransactions();
        assertEquals(2, list.size(), "There should still be 2 transactions");

        Transaction t = list.get(1);
        assertEquals("Rent", t.getDescription(), "Description should be updated");
        assertEquals(new BigDecimal("400.00"), t.getAmount(), "Amount should be updated");
        assertEquals(LocalDate.parse("2025-07-03"), t.getDate(), "Date should be updated");
        assertEquals(Category.OTHER, t.getCategory(), "Category should be updated");
        assertFalse(t.isIncome(), "Should be updated to not Income");

        assertTrue(manager.hasUnsavedChanges(), "Manager should be dirty after updating transaction");
    }

    //----------deleteTransaction---------------
    @Test
    void shouldDeleteCorrectly(){
        manager.addTransaction(salary);
        manager.addTransaction(rent);

        manager.deleteTransaction(1);

        List<Transaction> list = manager.getTransactions();
        assertEquals(1, list.size(), "List of transactions should reduce by 1");

        assertTrue(manager.hasUnsavedChanges(), "Should be marked dirty after deletion");
    }

    //----------getTransaction---------------
    @Test
    void shouldListAllTransactions(){
        manager.addTransaction(salary);
        manager.addTransaction(salary2);

        List<Transaction> list = manager.getTransactions();

        assertEquals(2, list.size(), "Size of returned list should be 2");

        Transaction t = list.get(0);
        assertEquals("Salary1", t.getDescription(), "Description should match");
        assertEquals(new BigDecimal("1000.00"), t.getAmount(), "Amount should match");
        assertEquals(LocalDate.parse("2025-07-01"), t.getDate(), "Date should match");
        assertEquals(Category.INCOME, t.getCategory(), "Category should match");
        assertTrue(t.isIncome(), "Income flag should match");

        Transaction t2 = list.get(1);
        assertEquals("Salary2", t2.getDescription(), "Description should match");
        assertEquals(new BigDecimal("2000.00"), t2.getAmount(), "Amount should match");
        assertEquals(LocalDate.parse("2025-07-02"), t2.getDate(), "Date should match");
        assertEquals(Category.INCOME, t2.getCategory(), "Category should match");
        assertTrue(t2.isIncome(), "Income flag should match");
    }

    @Test
    void shouldHandleEmptyList(){
        List<Transaction> list = manager.getTransactions();
        assertEquals(0, list.size(), "List is empty! Size should be 0");
    }

    //----------getBalance---------------
    @Test
    void balanceReflectsIncomeandExpense(){

        manager.addTransaction(salary);
        manager.addTransaction(rent);
                                            
        assertEquals(new BigDecimal("600.00"), manager.getBalance(), "Balance should be +600.00");
    }

    @Test
    void allIncomeBalanceCheck(){

        manager.addTransaction(salary);
        manager.addTransaction(salary2);

        assertEquals(new BigDecimal("3000.00"), manager.getBalance(), "Balance should be +3000.00");
    }

    @Test
    void noIncomeWithExpense(){

        manager.addTransaction(food);
        manager.addTransaction(food2);

        assertEquals(new BigDecimal("-3000.00"), manager.getBalance(), "Balance should be -3000.00");
    }

    @Test
    void noTransactions(){

        assertEquals(new BigDecimal("0.00"), manager.getBalance(), "Balance should be zero?");
    }


    //----------inMonth---------------
    @Test
    void checkTransactionsInMonth(){
        manager.addTransaction(salary);
        manager.addTransaction(rent);
        manager.addTransaction(food);

        List<Transaction> list = manager.inMonth(YearMonth.of(2025, 7));

        assertEquals(2, list.size(), "There should only be 2 transactions in that month");

        Transaction t1 = list.get(0);
        Transaction t2 = list.get(1);

        assertEquals(LocalDate.parse("2025-07-01"), t1.getDate(), "Transaction should be from 07-2025");
        assertEquals(LocalDate.parse("2025-07-03"), t2.getDate(), "Transaction should be from 07-2025");

    }

    @Test
    void noTransactionsInMonth(){
        List<Transaction> list = manager.inMonth(YearMonth.of(2025, 8));

        assertEquals(0, list.size(), "List of transactions in that month should be empty");
    }
    
    //----------inCategory---------------
    @Test
    void checkTransactionOfCategory(){
        manager.addTransaction(salary);
        manager.addTransaction(rent);
        manager.addTransaction(salary2);

        List<Transaction> list = manager.inCategory(Category.INCOME);

        assertEquals(2, list.size());

        Transaction t1 = list.get(0);
        Transaction t2 = list.get(1);

        assertEquals(Category.INCOME, t1.getCategory(), "Category should be INCOME");
        assertEquals(Category.INCOME, t2.getCategory(), "Category should be INCOME");
    }

    @Test
    void noTransactionsOfCategory(){
        List<Transaction> list = manager.inCategory(Category.RENT);

        assertEquals(0, list.size(), "There should be no transactions for that Category");
    }

    //----------summarizeByCategory---------------
    @Test
    void correctSumPerMonth(){

        manager.addTransaction(salary);
        manager.addTransaction(food);
        manager.addTransaction(food2);

        //List<Transaction> subset = manager.inMonth(YearMonth.of(2025, 8));

        Map<Category, BigDecimal> map = manager.summariseByCategory(manager.getTransactions());

        assertEquals(new BigDecimal("3000.00"), map.get(Category.FOOD), "Food total should be 3000.00");
        assertEquals(new BigDecimal("1000.00"), map.get(Category.INCOME), "Income total should be 1000.00");
    }



    //helper
    private Transaction tx (String disc, String amount, String date, Category cat, boolean isIncome){
        return new Transaction(disc, new BigDecimal(amount), LocalDate.parse(date), cat, isIncome);
    }


}
