package com.trackviro.backend.dto.limitrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Matches LimitRequestController's old "/employee/limitRequest"
 * @RequestParam Double requestedAmount, @RequestParam String reason.
 */
public record LimitRequestCreateRequest(
        @NotNull(message = "Requested amount is required")
        @Positive(message = "Requested amount must be greater than zero")
        Double requestedAmount,

        @NotBlank(message = "A reason is required")
        String reason
) {}
