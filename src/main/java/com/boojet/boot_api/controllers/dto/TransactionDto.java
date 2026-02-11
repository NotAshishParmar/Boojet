package com.boojet.boot_api.controllers.dto;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Category;

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

    private Long categoryId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String categoryCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String categoryName;

    private Long accountId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean income;
}
