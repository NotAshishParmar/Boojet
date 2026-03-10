package com.boojet.boot_api.dto.category;

import com.fasterxml.jackson.databind.JsonNode;

public record CategoryPatchRequest(
        String code,
        String name,
        Boolean essential,
        Integer sortOrder,
        JsonNode parentId       //TRI-STATE: if changed then set new parent, if set to null then set parent to null, if no change then do not touch parent
) {}