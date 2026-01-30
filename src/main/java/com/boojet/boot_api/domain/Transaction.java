package com.boojet.boot_api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a single ledger entry (income or expense) recorded against an {@link Account}.
 * <p>
 * A transaction has an amount, date, category, and a flag indicating whether it is income.
 * This entity is used to compute monthly summaries, category totals, and net reports.
 *
 * <p><b>Money persistence:</b>
 * The {@link #amount} field is persisted as a {@link java.math.BigDecimal} via {@link MoneyConverter}.
 *
 * <p><b>Income vs Expense:</b>
 * The {@link #income} flag indicates whether the transaction should be treated as income ({@code true})
 * or expense ({@code false}). (This is independent of the numeric sign of {@link #amount}.)
 */
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryEnum category;

    @JsonProperty("income")
    @Column(name = "is_income", nullable = false)       // DB uses "is_income"
    private boolean income;

    @ManyToOne(optional = false)
    private Account account;                            // The account associated with this transaction

    /**
     * Convenience constructor for creating a transaction without an id.
     *
     * @param description description of the transaction
     * @param amount transaction amount
     * @param date transaction date
     * @param category transaction category
     * @param income whether the transaction is income ({@code true}) or expense ({@code false})
     * @param account owning account
     */
    public Transaction(String description, Money amount, LocalDate date, CategoryEnum category, boolean income, Account account){
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.income = income;
        this.account = account;
    }

    @Override
    public String toString(){
        String type = income ? "Income" : "Expense";
        return "[" + date + "] " + type + ": " + amount + " | " + category + " | " + description + " (Account: " + account.getName() + ")";
    }

}