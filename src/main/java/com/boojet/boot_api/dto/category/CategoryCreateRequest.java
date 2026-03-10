package com.boojet.boot_api.dto.category;

import com.boojet.boot_api.domain.CategoryType;

public record CategoryCreateRequest(
        String code,
        String name,
        CategoryType type,
        Boolean essential,
        Integer sortOrder,
        Long parentId
) {}