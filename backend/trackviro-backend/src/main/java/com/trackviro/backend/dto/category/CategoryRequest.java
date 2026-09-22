package com.trackviro.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** Matches FinanceController's old "/category/save" form (Category c). */
public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,

        @PositiveOrZero(message = "Max limit cannot be negative")
        Double maxLimit
) {}
