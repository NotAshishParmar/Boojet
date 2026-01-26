package com.boojet.boot_api.controllers.dto;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;

public record TxSuggestionDetails(
    String description,
    Category category,
    Money amount,
    boolean income,
    Long accountId
) {} 

