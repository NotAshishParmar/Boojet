package com.boojet.boot_api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.math.BigDecimal;

public class Transaction{

    @JsonProperty
    private String description;
    @JsonProperty
    private Money amount;
    @JsonProperty
    private LocalDate date;
    @JsonProperty
    private Category category;
    @JsonProperty
    private boolean isIncome;

    //required default constructor for Jackson
    public Transaction() {}

    public Transaction(String description, Money amount, LocalDate date, Category category, boolean isIncome){
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.isIncome = isIncome;
    }

    //Getters
    public String getDescription(){ return description; }
    public Money getAmount(){ return amount; }
    public LocalDate getDate(){ return date; }
    public Category getCategory() { return category; }
    @JsonProperty("isIncome")
    public boolean isIncome() { return isIncome; }


    @Override
    public String toString(){
        String type = isIncome ? "Income" : "Expense";
        return "[" + date + "] " + type + ": " + amount + " | " + category + " | " + description;
    }

    private static String formatCurrency(BigDecimal value){
        return java.text.NumberFormat.getCurrencyInstance().format(value);
    }
}