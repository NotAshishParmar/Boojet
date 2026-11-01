package com.boojet.boot_api.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "loan_details")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class LoanDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne (optional = false)
    @JoinColumn (name = "account_id", nullable = false, unique = true)
    private Account linkedAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private Money originalPrincipal;

    @Column (nullable = false, precision = 8, scale = 4)
    private BigDecimal annualInterestRate;

    @Column (nullable = false)
    private int termMonths;
}
