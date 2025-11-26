package com.boojet.boot_api.controllers.dto;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDto {
    
    private Long id;
    private String description;
    private Money amount;
    private LocalDate date;
    private Category category;
    private boolean income;
    private Account Account;
}
