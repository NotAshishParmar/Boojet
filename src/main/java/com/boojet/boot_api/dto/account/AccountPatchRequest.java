package com.boojet.boot_api.dto.account;

import java.time.LocalDate;

public record AccountPatchRequest (
    String name,
    LocalDate closedAt          //can apply Tri-state later
) {}
