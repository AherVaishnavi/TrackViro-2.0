package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.expense.ExpenseRequest;
import com.trackviro.backend.dto.expense.ExpenseResponse;
import com.trackviro.backend.exception.AccessDeniedForResourceException;
import com.trackviro.backend.exception.BusinessRuleException;
import com.trackviro.backend.exception.ResourceNotFoundException;
import com.trackviro.backend.model.Category;
import com.trackviro.backend.model.Department;
import com.trackviro.backend.model.Expense;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.CategoryRepository;
import com.trackviro.backend.repository.ExpenseRepository;
import com.trackviro.backend.repository.UserRepository;
import com.trackviro.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExpenseServiceImpl.
 *
 * These mock every repository/collaborator instead of talking to
 * MySQL — @ExtendWith(MockitoExtension.class) is a plain unit test,
 * no Spring context is started, so these run in milliseconds and
 * exercise exactly the branching logic in the service itself.
 *
 * Covers the two most interesting rules in the whole application:
 * the duplicate/limit checks on submitExpense, and the department
 * object-level authorization on approveByManager (the fix documented
 * in ExpenseServiceImpl's own Javadoc).
 */
@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private User employee;
    private Category category;
    private ExpenseRequest request;

    @BeforeEach
    void setUp() {
        Department dept = new Department();
        dept.setId(10L);
        dept.setName("Engineering");

        employee = new User();
        employee.setId(1L);
        employee.setName("Asha Rao");
        employee.setMonthlyLimit(10000.0);
        employee.setDepartment(dept);

        category = new Category();
        category.setId(5L);
        category.setName("Travel");

        request = new ExpenseRequest(LocalDate.of(2026, 9, 1), 5L, 2000.0, "Client visit");
    }

    // ── submitExpense ─────────────────────────────────────────

    @Test
    void submitExpense_savesAndReturnsPendingExpense_whenNoDuplicateAndUnderLimit() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(expenseRepository.existsByEmployeeAndExpenseDateAndAmountAndCategoryId(
                employee, request.expenseDate(), request.amount(), 5L)).thenReturn(false);
        when(expenseRepository.getMonthlyTotal(1L)).thenReturn(1000.0); // already spent this month

        // save() just needs to return whatever was passed in, with a
        // generated id — a common Mockito pattern for repositories.
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense e = invocation.getArgument(0);
            e.setId(99L);
            return e;
        });

        ExpenseResponse response = expenseService.submitExpense(1L, request, null);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.amount()).isEqualTo(2000.0);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void submitExpense_throwsResourceNotFound_whenEmployeeDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.submitExpense(1L, request, null))
                .isInstanceOf(ResourceNotFoundException.class);

        // No expense should ever be saved if the employee lookup failed.
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void submitExpense_throwsDuplicateBusinessRuleException_whenSameExpenseAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(expenseRepository.existsByEmployeeAndExpenseDateAndAmountAndCategoryId(
                employee, request.expenseDate(), request.amount(), 5L)).thenReturn(true);

        assertThatThrownBy(() -> expenseService.submitExpense(1L, request, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode()).isEqualTo("DUPLICATE"));

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void submitExpense_throwsLimitExceededBusinessRuleException_whenOverMonthlyLimit() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(expenseRepository.existsByEmployeeAndExpenseDateAndAmountAndCategoryId(
                any(), any(), any(), any())).thenReturn(false);
        // Already spent 9000 of a 10000 limit; this 2000 expense would push past it.
        when(expenseRepository.getMonthlyTotal(1L)).thenReturn(9000.0);

        assertThatThrownBy(() -> expenseService.submitExpense(1L, request, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode()).isEqualTo("LIMIT_EXCEEDED"));

        verify(expenseRepository, never()).save(any());
    }

    // ── approveByManager (object-level authorization) ───────────

    @Test
    void approveByManager_approvesAndNotifiesEmployee_whenSameDepartment() {
        Expense expense = buildExpense(employee, category, "PENDING");
        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseResponse response = expenseService.approveByManager(50L, 10L); // same dept id (10L)

        assertThat(response.status()).isEqualTo("MANAGER_APPROVED");
        verify(notificationService).createNotification(eq(employee), anyString(), eq("MANAGER_APPROVED"));
    }

    @Test
    void approveByManager_throwsAccessDenied_whenManagerIsFromDifferentDepartment() {
        Expense expense = buildExpense(employee, category, "PENDING"); // employee is in dept 10
        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.approveByManager(50L, 999L)) // different dept
                .isInstanceOf(AccessDeniedForResourceException.class);

        // The whole point of the check is that nothing gets saved or
        // notified if the department doesn't match.
        verify(expenseRepository, never()).save(any());
        verify(notificationService, never()).createNotification(any(), any(), any());
    }

    @Test
    void getEmployeeExpenses_returnsMappedExpenseResponses() {
        Expense e1 = buildExpense(employee, category, "PENDING");
        e1.setId(1L);
        Expense e2 = buildExpense(employee, category, "REIMBURSED");
        e2.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(expenseRepository.findByEmployeeAndIsActiveTrue(employee)).thenReturn(List.of(e1, e2));

        List<ExpenseResponse> result = expenseService.getEmployeeExpenses(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).status()).isEqualTo("PENDING");
        assertThat(result.get(1).status()).isEqualTo("REIMBURSED");
    }

    // ── Helpers ───────────────────────────────────────────────

    private Expense buildExpense(User owner, Category cat, String status) {
        Expense e = new Expense();
        e.setEmployee(owner);
        e.setCategory(cat);
        e.setAmount(1500.0);
        e.setExpenseDate(LocalDate.of(2026, 9, 10));
        e.setStatus(status);
        e.setIsActive(true);
        return e;
    }
}
