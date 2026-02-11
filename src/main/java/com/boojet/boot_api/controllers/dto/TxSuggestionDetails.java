package com.boojet.boot_api.controllers.dto;

import com.boojet.boot_api.domain.Money;

public record TxSuggestionDetails(
    String description,
    Long categoryId,
    Money amount,
    boolean income,
    Long accountId
) {} 

