package com.boojet.boot_api.controllers.dto;

import com.boojet.boot_api.domain.CategoryType;
import com.fasterxml.jackson.databind.JsonNode;

public record CategoryPatchRequest(
        String code,
        String name,
        CategoryType type,
        Boolean essential,
        Integer sortOrder,
        JsonNode parentId                   //if changed then set new parent, if set to null then set parent to null, if no change then do not touch parent
) {}