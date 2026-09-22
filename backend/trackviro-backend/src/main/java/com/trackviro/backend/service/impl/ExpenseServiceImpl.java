package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.common.ChartData;
import com.trackviro.backend.dto.expense.ExpenseRequest;
import com.trackviro.backend.dto.expense.ExpenseResponse;
import com.trackviro.backend.exception.AccessDeniedForResourceException;
import com.trackviro.backend.exception.BusinessRuleException;
import com.trackviro.backend.exception.ResourceNotFoundException;
import com.trackviro.backend.model.Category;
import com.trackviro.backend.model.Expense;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.CategoryRepository;
import com.trackviro.backend.repository.ExpenseRepository;
import com.trackviro.backend.repository.UserRepository;
import com.trackviro.backend.service.ExpenseService;
import com.trackviro.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Ported from com.example.demo.service.impl.ExpenseServiceImpl.
 *
 * Business logic preserved exactly: the duplicate check, the monthly
 * limit check, every status transition, and every notification
 * message string. Two things were added beyond a straight port, per
 * Step 4 instructions:
 *
 *  1. Object-level authorization on approveByManager/rejectByManager —
 *     the old app let any manager mutate any department's expense by
 *     changing the ID in the request. assertSameDepartment() below is
 *     new; everything else in this class is a port.
 *  2. @Transactional on every write method — added for safety, does
 *     not change outcomes under normal operation.
 *
 * findById(id).get() is replaced by findById(id).orElseThrow(...) ->
 * ResourceNotFoundException everywhere, replacing the old app's raw
 * NoSuchElementException on a bad ID.
 *
 * NOT changed here, per Step 4 scope (business-logic bugs, not this
 * step's job): getMonthlyTotal still has no YEAR()/isActive filter,
 * and the currency amounts are still Double, not BigDecimal.
 */
@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private NotificationService notificationService;

    // ── Employee ──────────────────────────────────────────────

    @Override
    @Transactional
    public ExpenseResponse submitExpense(Long employeeId, ExpenseRequest request, String billPath) {

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        boolean exists = expenseRepository.existsByEmployeeAndExpenseDateAndAmountAndCategoryId(
                employee, request.expenseDate(), request.amount(), category.getId());

        if (exists) {
            throw new BusinessRuleException("DUPLICATE",
                    "An identical expense already exists for this date, amount, and category.",
                    HttpStatus.CONFLICT);
        }

        Double monthlyTotal = expenseRepository.getMonthlyTotal(employee.getId());
        if (monthlyTotal == null) monthlyTotal = 0.0;

        if (monthlyTotal + request.amount() > employee.getMonthlyLimit()) {
            throw new BusinessRuleException("LIMIT_EXCEEDED",
                    "This expense would exceed your monthly limit.",
                    HttpStatus.BAD_REQUEST);
        }

        Expense expense = new Expense();
        expense.setEmployee(employee);
        expense.setCategory(category);
        expense.setExpenseDate(request.expenseDate());
        expense.setAmount(request.amount());
        expense.setDescription(request.description());
        expense.setBillPath(billPath);
        expense.setStatus("PENDING");
        expense.setIsActive(true);

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Override
    public List<ExpenseResponse> getEmployeeExpenses(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));
        return expenseRepository.findByEmployeeAndIsActiveTrue(employee)
                .stream().map(ExpenseResponse::from).toList();
    }

    @Override
    public Double getMonthlyTotal(Long empId) {
        return expenseRepository.getMonthlyTotal(empId);
    }

    @Override
    public Long getStatusCount(Long empId, String status) {
        return expenseRepository.countByStatus(empId, status);
    }

    @Override
    public Double getApprovedAmount(Long empId) {
        return expenseRepository.getApprovedAmount(empId);
    }

    @Override
    public Double getReimbursedAmount(Long empId) {
        return expenseRepository.getReimbursedAmount(empId);
    }

    @Override
    public ChartData getEmpCategorySummary(Long empId) {
        return ChartData.from(expenseRepository.getEmpCategorySummary(empId));
    }

    @Override
    public ChartData getEmpMonthlySummary(Long empId) {
        return ChartData.from(expenseRepository.getEmpMonthlySummary(empId));
    }

    // ── Manager ───────────────────────────────────────────────

    @Override
    public List<ExpenseResponse> getDepartmentExpenses(Long deptId) {
        return expenseRepository.findByEmployeeDepartmentIdAndStatusAndIsActiveTrue(deptId, "PENDING")
                .stream().map(ExpenseResponse::from).toList();
    }

    @Override
    public List<ExpenseResponse> getAllDepartmentExpenses(Long deptId) {
        return expenseRepository.findAllByDepartmentId(deptId)
                .stream().map(ExpenseResponse::from).toList();
    }

    @Override
    public List<ExpenseResponse> getManagerApproved() {
        return expenseRepository.findByStatusAndIsActiveTrue("MANAGER_APPROVED")
                .stream().map(ExpenseResponse::from).toList();
    }

    @Override
    @Transactional
    public ExpenseResponse approveByManager(Long expenseId, Long actingManagerDeptId) {
        Expense e = loadExpense(expenseId);
        assertSameDepartment(e, actingManagerDeptId);

        e.setStatus("MANAGER_APPROVED");
        Expense saved = expenseRepository.save(e);

        notificationService.createNotification(
                saved.getEmployee(),
                "Your expense of ₹" + saved.getAmount() + " (" + saved.getCategory().getName()
                        + ") was approved by your Manager.",
                "MANAGER_APPROVED");

        return ExpenseResponse.from(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse rejectByManager(Long expenseId, Long actingManagerDeptId, String reason) {
        Expense e = loadExpense(expenseId);
        assertSameDepartment(e, actingManagerDeptId);

        e.setStatus("REJECTED");
        e.setRejectReason(reason);
        Expense saved = expenseRepository.save(e);

        notificationService.createNotification(
                saved.getEmployee(),
                "Your expense of ₹" + saved.getAmount() + " (" + saved.getCategory().getName()
                        + ") was rejected. Reason: " + reason,
                "REJECTED");

        return ExpenseResponse.from(saved);
    }

    @Override
    public Long getDeptStatusCount(Long deptId, String status) {
        return expenseRepository.countDeptByStatus(deptId, status);
    }

    @Override
    public ChartData getDeptCategorySummary(Long deptId) {
        return ChartData.from(expenseRepository.getDeptCategorySummary(deptId));
    }

    @Override
    public ChartData getDeptMonthlySummary(Long deptId) {
        return ChartData.from(expenseRepository.getDeptMonthlySummary(deptId));
    }

    // ── Finance — global scope, unrestricted (matches the old app) ─

    @Override
    public List<ExpenseResponse> getFinalApproved() {
        return expenseRepository.findByStatusAndIsActiveTrue("FINANCE_APPROVED")
                .stream().map(ExpenseResponse::from).toList();
    }

    @Override
    @Transactional
    public ExpenseResponse finalApprove(Long expenseId) {
        Expense e = loadExpense(expenseId);
        e.setStatus("FINANCE_APPROVED");
        Expense saved = expenseRepository.save(e);

        notificationService.createNotification(
                saved.getEmployee(),
                "Your expense of ₹" + saved.getAmount() + " (" + saved.getCategory().getName()
                        + ") received Finance approval.",
                "FINANCE_APPROVED");

        return ExpenseResponse.from(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse reimburse(Long expenseId) {
        Expense e = loadExpense(expenseId);
        e.setStatus("REIMBURSED");
        e.setReimbursementDate(LocalDate.now());
        Expense saved = expenseRepository.save(e);

        notificationService.createNotification(
                saved.getEmployee(),
                "Your expense of ₹" + saved.getAmount() + " (" + saved.getCategory().getName()
                        + ") has been reimbursed on " + LocalDate.now() + ".",
                "REIMBURSED");

        return ExpenseResponse.from(saved);
    }

    @Override
    @Transactional
    public void softDelete(Long expenseId) {
        Expense e = loadExpense(expenseId);
        e.setIsActive(false);
        expenseRepository.save(e);
    }

    @Override
    public Double getFinanceMonthlyApproved() {
        return expenseRepository.monthlyFinanceApproved();
    }

    @Override
    public Double getFinanceTotalReimbursed() {
        return expenseRepository.totalReimbursed();
    }

    @Override
    public Long getTotalStatusCount(String status) {
        return expenseRepository.countAllByStatus(status);
    }

    @Override
    public ChartData getCategorySummary() {
        return ChartData.from(expenseRepository.categorySummary());
    }

    @Override
    public ChartData getDeptSummary() {
        return ChartData.from(expenseRepository.getDeptSummary());
    }

    @Override
    public ChartData getMonthlyTrend() {
        return ChartData.from(expenseRepository.getMonthlyTrend());
    }

    @Override
    public ChartData getTopSpenders() {
        return ChartData.from(expenseRepository.getTopSpenders());
    }

    // ── Helpers ───────────────────────────────────────────────

    private Expense loadExpense(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    /**
     * The object-level authorization check the old app never had:
     * ManagerController.approve/reject looked up an expense by ID with
     * no verification the expense belonged to that manager's own
     * department. Any manager could approve any other department's
     * expense by changing the ID in the form POST. This method closes
     * that gap.
     */
    private void assertSameDepartment(Expense expense, Long actingManagerDeptId) {
        Long expenseDeptId = expense.getEmployee() != null && expense.getEmployee().getDepartment() != null
                ? expense.getEmployee().getDepartment().getId()
                : null;

        if (actingManagerDeptId == null || !actingManagerDeptId.equals(expenseDeptId)) {
            throw new AccessDeniedForResourceException(
                    "You can only act on expenses from your own department.");
        }
    }
}
