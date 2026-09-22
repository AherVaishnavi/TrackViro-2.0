package com.trackviro.backend.dto.user;

import jakarta.validation.constraints.*;

/**
 * Matches the fields FinanceController's old "/user/save" form bound
 * directly onto a User entity via @ModelAttribute. Field names kept
 * identical to the entity so the business meaning doesn't shift.
 * role is still a free-form String here, matching Step 3 instruction #9
 * (existing field names/meaning preserved) — the old UI only offered
 * EMPLOYEE/MANAGER radios, but the entity itself never constrained it.
 */
public record UserCreateRequest(
        @NotBlank(message = "Employee code is required")
        String employeeCode,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 4, message = "Password must be at least 4 characters")
        String password,

        String phone,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "EMPLOYEE|MANAGER|FINANCE", message = "Role must be EMPLOYEE, MANAGER, or FINANCE")
        String role,

        Long departmentId,

        @PositiveOrZero(message = "Monthly limit cannot be negative")
        Double monthlyLimit
) {}
