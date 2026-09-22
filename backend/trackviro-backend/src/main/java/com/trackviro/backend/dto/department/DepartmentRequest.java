package com.trackviro.backend.dto.department;

import jakarta.validation.constraints.NotBlank;

/**
 * Matches FinanceController's old "/department/save" form. The old
 * department.html form only ever submitted "name" — no manager picker
 * existed in the UI — so that is the only field here, preserving the
 * old app's actual scope exactly (not adding new functionality).
 */
public record DepartmentRequest(
        @NotBlank(message = "Department name is required")
        String name
) {}
