package com.trackviro.backend.dto.category;

import com.trackviro.backend.model.Category;

public record CategoryResponse(Long id, String name, Double maxLimit) {
    public static CategoryResponse from(Category c) {
        if (c == null) return null;
        return new CategoryResponse(c.getId(), c.getName(), c.getMaxLimit());
    }
}
