package com.trackviro.backend.dto.limitrequest;

import com.trackviro.backend.model.LimitRequest;
import java.time.LocalDateTime;

/**
 * status values preserved exactly from the entity's documented lifecycle:
 * PENDING → MANAGER_APPROVED → APPROVED, or → REJECTED at either stage.
 */
public record LimitRequestResponse(
        Long id,
        Double requestedAmount,
        String reason,
        String status,
        String rejectReason,
        Double approvedAmount,
        LocalDateTime createdAt,
        Long employeeId,
        String employeeName,
        Long departmentId,
        String departmentName
) {
    public static LimitRequestResponse from(LimitRequest r) {
        if (r == null) return null;
        boolean hasEmployee = r.getEmployee() != null;
        boolean hasDept = hasEmployee && r.getEmployee().getDepartment() != null;

        return new LimitRequestResponse(
                r.getId(),
                r.getRequestedAmount(),
                r.getReason(),
                r.getStatus(),
                r.getRejectReason(),
                r.getApprovedAmount(),
                r.getCreatedAt(),
                hasEmployee ? r.getEmployee().getId()   : null,
                hasEmployee ? r.getEmployee().getName() : null,
                hasDept ? r.getEmployee().getDepartment().getId()   : null,
                hasDept ? r.getEmployee().getDepartment().getName() : null
        );
    }
}
