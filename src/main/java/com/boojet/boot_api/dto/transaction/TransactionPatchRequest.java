package com.boojet.boot_api.dto.transaction;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;
import com.fasterxml.jackson.databind.JsonNode;

public record TransactionPatchRequest(
    String description,
    Money amount,
    LocalDate date,
    Long categoryId,
    Long accountId,
    JsonNode toAccountId    //TRI-STATE; field not provided -> dont change, null -> clear toAccount, isNumber -> change toAccount
) {}
