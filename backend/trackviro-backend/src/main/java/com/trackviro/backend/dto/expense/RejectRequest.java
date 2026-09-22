package com.trackviro.backend.dto.expense;

import jakarta.validation.constraints.NotBlank;

/**
 * Matches ManagerController's old "/reject/{id}" @RequestParam String reason.
 * Also reused by LimitRequest rejection (manager and finance), which took
 * an identical single "reason" param in LimitRequestController.
 */
public record RejectRequest(
        @NotBlank(message = "A reason is required to reject this")
        String reason
) {}
