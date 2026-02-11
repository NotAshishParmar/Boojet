package com.boojet.boot_api.controllers.dto;

import com.boojet.boot_api.domain.CategoryType;

public record CategoryResponse(
    Long id,
    String code,
    String name,
    CategoryType type,
    Boolean essential,
    Integer sortOrder,
    boolean system,
    boolean active,
    Long parentId,
    String parentCode,
    boolean hasChildren
) {}
