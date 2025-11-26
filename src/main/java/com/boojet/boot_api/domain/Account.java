package com.boojet.boot_api.domain;

import java.time.LocalDate;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","name"}))
@Builder
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)                // Many accounts can belong to one user
    private User user;

    @Column(nullable = false)                   // Account name must be unique per user
    private String name;                        

    @Enumerated(EnumType.STRING)                // Store enum as string in DB
    @Column(nullable = false)                   // Account type is required
    @Builder.Default
    private AccountType type = AccountType.CHEQUING;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private Money openingBalance = Money.zero();  // Default to zero balance

    @Builder.Default
    private LocalDate createdAt = LocalDate.now();
    private LocalDate closedAt;                  // Null if account is active


    public boolean isActive(){
        return closedAt == null;
    }

}
