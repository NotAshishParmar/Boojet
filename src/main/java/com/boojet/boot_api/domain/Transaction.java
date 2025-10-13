package com.boojet.boot_api.domain;

import com.boojet.boot_api.Category;
import com.boojet.boot_api.Money;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transaction{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_id_seq")
    private Long id;

    @JsonProperty
    private String description;
    @JsonProperty
    private Money amount;
    @JsonProperty
    private LocalDate date;
    @JsonProperty
    private Category category;
    @JsonProperty
    private Boolean isIncome;

    public Transaction(String description, Money amount, LocalDate date, Category category, boolean isIncome){
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.isIncome = isIncome;
    }

    //for Jackson
    @JsonProperty("isIncome")
    public boolean isIncome() { return isIncome; }


    @Override
    public String toString(){
        String type = isIncome ? "Income" : "Expense";
        return "[" + date + "] " + type + ": " + amount + " | " + category + " | " + description;
    }

}