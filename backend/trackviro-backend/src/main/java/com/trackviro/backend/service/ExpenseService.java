package com.trackviro.backend.service;

import com.trackviro.backend.dto.common.ChartData;
import com.trackviro.backend.dto.expense.ExpenseRequest;
import com.trackviro.backend.dto.expense.ExpenseResponse;

import java.util.List;

/**
 * Ported from com.example.demo.service.ExpenseService, adapted to DTOs.
 * Method names kept identical to the old interface wherever the old
 * name still made sense, so this reads as a direct 1:1 mapping.
 */
public interface ExpenseService {

    // ── Employee ──────────────────────────────────────────────
    ExpenseResponse submitExpense(Long employeeId, ExpenseRequest request, String billPath);
    List<ExpenseResponse> getEmployeeExpenses(Long employeeId);
    Double getMonthlyTotal(Long empId);
    Long getStatusCount(Long empId, String status);
    Double getApprovedAmount(Long empId);
    Double getReimbursedAmount(Long empId);
    ChartData getEmpCategorySummary(Long empId);
    ChartData getEmpMonthlySummary(Long empId);

    // ── Manager. actingManagerDeptId is checked against the
    //    expense's own employee's department before any mutation —
    //    the object-level authorization the old app was missing. ──
    List<ExpenseResponse> getDepartmentExpenses(Long deptId);
    List<ExpenseResponse> getAllDepartmentExpenses(Long deptId);
    List<ExpenseResponse> getManagerApproved();
    ExpenseResponse approveByManager(Long expenseId, Long actingManagerDeptId);
    ExpenseResponse rejectByManager(Long expenseId, Long actingManagerDeptId, String reason);
    Long getDeptStatusCount(Long deptId, String status);
    ChartData getDeptCategorySummary(Long deptId);
    ChartData getDeptMonthlySummary(Long deptId);

    // ── Finance — global scope, unrestricted, matching the old app ─
    List<ExpenseResponse> getFinalApproved();
    ExpenseResponse finalApprove(Long expenseId);
    ExpenseResponse reimburse(Long expenseId);
    void softDelete(Long expenseId);
    Double getFinanceMonthlyApproved();
    Double getFinanceTotalReimbursed();
    Long getTotalStatusCount(String status);
    ChartData getCategorySummary();
    ChartData getDeptSummary();
    ChartData getMonthlyTrend();
    ChartData getTopSpenders();
}
