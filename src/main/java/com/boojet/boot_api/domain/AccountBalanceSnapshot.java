package com.boojet.boot_api.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "account_balance_snapshots",
    uniqueConstraints = @UniqueConstraint(name = "uq_account_snapshot", columnNames = {"account_id", "as_of_date"})
)
@Builder
public class AccountBalanceSnapshot {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "balance_amount", nullable = false, precision = 19, scale = 4)
    @Convert(converter = MoneyConverter.class)
    private Money balanceAmount;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
