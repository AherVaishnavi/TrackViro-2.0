package com.trackviro.backend.dto.expense;

import com.trackviro.backend.model.Expense;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Flattened — no nested User/Category/Department objects, which
 * avoids the Expense → User → Department → manager → Department
 * circular reference the entity graph would otherwise produce if
 * serialized directly (Step 3 req #5).
 *
 * status values preserved exactly from the entity's documented
 * lifecycle: PENDING, MANAGER_APPROVED, FINANCE_APPROVED, REJECTED,
 * REIMBURSED.
 */
public record ExpenseResponse(
        Long id,
        LocalDate expenseDate,
        Double amount,
        String description,
        String billPath,
        String status,
        String rejectReason,
        LocalDate reimbursementDate,
        LocalDateTime createdAt,
        Long employeeId,
        String employeeName,
        Long categoryId,
        String categoryName,
        Long departmentId,
        String departmentName
) {
    public static ExpenseResponse from(Expense e) {
        if (e == null) return null;
        boolean hasEmployee = e.getEmployee() != null;
        boolean hasDept = hasEmployee && e.getEmployee().getDepartment() != null;
        boolean hasCategory = e.getCategory() != null;

        return new ExpenseResponse(
                e.getId(),
                e.getExpenseDate(),
                e.getAmount(),
                e.getDescription(),
                e.getBillPath(),
                e.getStatus(),
                e.getRejectReason(),
                e.getReimbursementDate(),
                e.getCreatedAt(),
                hasEmployee ? e.getEmployee().getId()   : null,
                hasEmployee ? e.getEmployee().getName() : null,
                hasCategory ? e.getCategory().getId()   : null,
                hasCategory ? e.getCategory().getName() : null,
                hasDept ? e.getEmployee().getDepartment().getId()   : null,
                hasDept ? e.getEmployee().getDepartment().getName() : null
        );
    }
}
