package com.trackviro.backend.dto.expense;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * Matches the old submit_expense.html form fields: expenseDate,
 * category.id (here categoryId), amount, description. billFile is
 * handled as a separate multipart part by the controller in a later
 * step, not part of this JSON body — same split EmployeeController's
 * old /save endpoint used (@ModelAttribute Expense + separate
 * @RequestParam MultipartFile).
 */
public record ExpenseRequest(
        @NotNull(message = "Expense date is required")
        LocalDate expenseDate,

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Double amount,

        String description
) {}
