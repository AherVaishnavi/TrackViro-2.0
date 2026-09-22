package com.trackviro.backend.dto.limitrequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Matches LimitRequestController's old "/finance/limitApprove/{id}"
 * @RequestParam Double approvedAmount — finance sets the actual amount
 * to add, which may differ from what the employee requested (preserved
 * exactly, per LimitRequestServiceImpl.approveByFinance).
 */
public record FinanceApproveRequest(
        @NotNull(message = "Approved amount is required")
        @Positive(message = "Approved amount must be greater than zero")
        Double approvedAmount
) {}
