package com.boojet.boot_api.dto.account;

import java.time.LocalDate;

public record AccountPutRequest(
    String name,
    LocalDate closedAt      //can apply Tri-state later
) {}
