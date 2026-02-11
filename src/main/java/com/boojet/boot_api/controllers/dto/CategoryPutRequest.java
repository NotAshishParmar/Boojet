package com.boojet.boot_api.controllers.dto;

public record CategoryPutRequest(
        String code,
        String name,
        Boolean essential,
        Integer sortOrder,
        Long parentId
) {}
