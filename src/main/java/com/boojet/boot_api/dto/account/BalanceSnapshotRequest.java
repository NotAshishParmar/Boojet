package com.boojet.boot_api.dto.account;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Money;

public record BalanceSnapshotRequest(
    LocalDate asOfDate,                     //optional, defaults to now()
    Money balance                           //required
) {}
