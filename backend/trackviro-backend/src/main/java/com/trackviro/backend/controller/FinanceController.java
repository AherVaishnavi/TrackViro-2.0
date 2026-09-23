package com.trackviro.backend.controller;

import com.trackviro.backend.dto.common.ApiMessage;
import com.trackviro.backend.dto.common.ChartData;
import com.trackviro.backend.dto.expense.ExpenseResponse;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.security.AuthUtil;
import com.trackviro.backend.service.ExpenseService;
import com.trackviro.backend.service.LimitRequestService;
import com.trackviro.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces com.example.demo.controller.FinanceController's
 * expense-related endpoints (finalApprove, reimburse, delete) and
 * dashboard. Department/Category/User CRUD, which lived in this same
 * class in the old app, are split out into their own
 * DepartmentController/CategoryController/UserController here — one
 * controller per resource, matching the pattern used everywhere else
 * in this project, rather than one large class covering five
 * unrelated resource types.
 *
 * Finance actions are global — no department scoping, matching the
 * old app's actual (intentional) behaviour. That's not an oversight;
 * it's the same unrestricted scope ExpenseServiceImpl already
 * implements (Step 4).
 */
@RestController
@RequestMapping("/api/finance")
@Tag(name = "Finance", description = "Requires role FINANCE. Global (unrestricted) final approval, reimbursement, analytics, dashboard.")
public class FinanceController {

    private final ExpenseService expenseService;
    private final LimitRequestService limitRequestService;
    private final UserService userService;
    private final AuthUtil authUtil;

    public FinanceController(ExpenseService expenseService,
                             LimitRequestService limitRequestService,
                             UserService userService,
                             AuthUtil authUtil) {
        this.expenseService = expenseService;
        this.limitRequestService = limitRequestService;
        this.userService = userService;
        this.authUtil = authUtil;
    }

    // ── Expenses ──────────────────────────────────────────────

    @GetMapping("/expenses/manager-approved")
    public ResponseEntity<List<ExpenseResponse>> getManagerApproved() {
        return ResponseEntity.ok(expenseService.getManagerApproved());
    }

    @GetMapping("/expenses/final-approved")
    public ResponseEntity<List<ExpenseResponse>> getFinalApproved() {
        return ResponseEntity.ok(expenseService.getFinalApproved());
    }

    @PatchMapping("/expenses/{id}/final-approve")
    public ResponseEntity<ExpenseResponse> finalApprove(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.finalApprove(id));
    }

    @PatchMapping("/expenses/{id}/reimburse")
    public ResponseEntity<ExpenseResponse> reimburse(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.reimburse(id));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<ApiMessage> softDelete(@PathVariable Long id) {
        expenseService.softDelete(id);
        return ResponseEntity.ok(new ApiMessage("Expense removed."));
    }

    // ── Analytics ─────────────────────────────────────────────

    @GetMapping("/analytics/category")
    public ResponseEntity<ChartData> getCategoryChart() {
        return ResponseEntity.ok(expenseService.getCategorySummary());
    }

    @GetMapping("/analytics/department")
    public ResponseEntity<ChartData> getDeptChart() {
        return ResponseEntity.ok(expenseService.getDeptSummary());
    }

    @GetMapping("/analytics/monthly-trend")
    public ResponseEntity<ChartData> getMonthlyTrend() {
        return ResponseEntity.ok(expenseService.getMonthlyTrend());
    }

    @GetMapping("/analytics/top-spenders")
    public ResponseEntity<ChartData> getTopSpenders() {
        return ResponseEntity.ok(expenseService.getTopSpenders());
    }

    // ── Dashboard ─────────────────────────────────────────────
    // Same Map-bundling approach as the other two dashboards, matching
    // the old FinanceController.dashboard()'s 18-attribute model.
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", UserResponse.from(authUtil.currentUser()));
        data.put("managerApprovedExpenses", expenseService.getManagerApproved());
        data.put("finalApprovedExpenses", expenseService.getFinalApproved());
        data.put("allUsers", userService.getAllActiveUsers());
        data.put("pendingCount", expenseService.getTotalStatusCount("PENDING"));
        data.put("managerApprovedCount", expenseService.getTotalStatusCount("MANAGER_APPROVED"));
        data.put("financeApprovedCount", expenseService.getTotalStatusCount("FINANCE_APPROVED"));
        data.put("reimbursedCount", expenseService.getTotalStatusCount("REIMBURSED"));
        data.put("rejectedCount", expenseService.getTotalStatusCount("REJECTED"));
        data.put("monthlyApproved", expenseService.getFinanceMonthlyApproved());
        data.put("totalReimbursed", expenseService.getFinanceTotalReimbursed());
        data.put("pendingLimitRequests", limitRequestService.getManagerApprovedRequests());
        data.put("categoryChart", expenseService.getCategorySummary());
        data.put("departmentChart", expenseService.getDeptSummary());
        data.put("monthlyTrendChart", expenseService.getMonthlyTrend());
        data.put("topSpendersChart", expenseService.getTopSpenders());
        return ResponseEntity.ok(data);
    }
}
