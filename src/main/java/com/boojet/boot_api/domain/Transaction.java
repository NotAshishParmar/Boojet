package com.boojet.boot_api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
    @Convert(converter = MoneyConverter.class)
    private Money amount;
    @JsonProperty
    private LocalDate date;
    @JsonProperty
    private Category category;
    @JsonProperty("income")
    @Column(name = "is_income", nullable = false)  // DB uses "is_income"
    private boolean income;

    public Transaction(String description, Money amount, LocalDate date, Category category, boolean income){
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.income = income;
    }

    // //for Jackson
    // @JsonProperty("isIncome")
    // public boolean isIncome() { return isIncome; }


    @Override
    public String toString(){
        String type = income ? "Income" : "Expense";
        return "[" + date + "] " + type + ": " + amount + " | " + category + " | " + description;
    }

}