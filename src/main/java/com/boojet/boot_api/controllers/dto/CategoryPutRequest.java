package com.boojet.boot_api.controllers.dto;

import com.boojet.boot_api.domain.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryPutRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull CategoryType type,
        Boolean essential,
        Integer sortOrder,
        Long parentId
) {}
