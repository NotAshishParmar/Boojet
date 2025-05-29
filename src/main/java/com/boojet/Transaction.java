package com.boojet;

import java.time.LocalDate;

public class Transaction{

    private String description;
    private double amount;
    private LocalDate date;
    private Category category;
    private boolean isIncome;

    public Transaction(String description, double amount, LocalDate date, Category category, boolean isIncome){
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.isIncome = isIncome;
    }

    //Getters
    public String getDescription(){ return description; }
    public double getAmount(){ return amount; }
    public LocalDate getDate(){ return date; }
    public Category getCategory() { return category; }
    public boolean isIncome() { return isIncome; }


    @Override
    public String toString(){
        String type = isIncome ? "Income" : "Expense";
        return "[" + date + "] " + type + ": $" + amount + " | " + category + " | " + description;
    }
}