package com.boojet.boot_api.dto.category;

public record CategoryPutRequest(
        String code,
        String name,
        Boolean essential,
        Integer sortOrder,
        Long parentId
) {}
