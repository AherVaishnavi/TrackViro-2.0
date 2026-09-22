package com.trackviro.backend.controller;

import com.trackviro.backend.dto.common.ChartData;
import com.trackviro.backend.dto.expense.ExpenseResponse;
import com.trackviro.backend.dto.expense.RejectRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestResponse;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.security.AuthUtil;
import com.trackviro.backend.service.ExpenseService;
import com.trackviro.backend.service.LimitRequestService;
import com.trackviro.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces com.example.demo.controller.ManagerController. Every
 * mutation here (approve/reject) passes authUtil.currentDepartmentId()
 * into the service layer, which is what enforces "a manager can only
 * act on their own department" (built in Step 4) — this is the fix
 * for the old app's missing object-level authorization: previously,
 * ManagerController.approve(id)/reject(id) trusted the path variable
 * alone, so any manager could act on any department's expense by
 * changing the ID.
 */
@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final ExpenseService expenseService;
    private final LimitRequestService limitRequestService;
    private final UserService userService;
    private final AuthUtil authUtil;

    public ManagerController(ExpenseService expenseService,
                             LimitRequestService limitRequestService,
                             UserService userService,
                             AuthUtil authUtil) {
        this.expenseService = expenseService;
        this.limitRequestService = limitRequestService;
        this.userService = userService;
        this.authUtil = authUtil;
    }

    // ── Expenses ──────────────────────────────────────────────

    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponse>> getPendingExpenses() {
        return ResponseEntity.ok(expenseService.getDepartmentExpenses(authUtil.currentDepartmentId()));
    }

    @GetMapping("/expenses/all")
    public ResponseEntity<List<ExpenseResponse>> getAllDepartmentExpenses() {
        return ResponseEntity.ok(expenseService.getAllDepartmentExpenses(authUtil.currentDepartmentId()));
    }

    @PatchMapping("/expenses/{id}/approve")
    public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(
                expenseService.approveByManager(id, authUtil.currentDepartmentId()));
    }

    @PatchMapping("/expenses/{id}/reject")
    public ResponseEntity<ExpenseResponse> reject(
            @PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(expenseService.rejectByManager(
                id, authUtil.currentDepartmentId(), request.reason()));
    }

    // ── Team ──────────────────────────────────────────────────

    @GetMapping("/team")
    public ResponseEntity<List<UserResponse>> getTeam() {
        return ResponseEntity.ok(userService.getEmployeesByDepartment(authUtil.currentDepartmentId()));
    }

    // ── Analytics ─────────────────────────────────────────────

    @GetMapping("/analytics/category")
    public ResponseEntity<ChartData> getCategoryChart() {
        return ResponseEntity.ok(expenseService.getDeptCategorySummary(authUtil.currentDepartmentId()));
    }

    @GetMapping("/analytics/monthly")
    public ResponseEntity<ChartData> getMonthlyChart() {
        return ResponseEntity.ok(expenseService.getDeptMonthlySummary(authUtil.currentDepartmentId()));
    }

    // ── Dashboard ─────────────────────────────────────────────
    // Same approach as EmployeeController.dashboard(): a plain Map
    // bundling existing DTOs, matching what the old
    // ManagerController.dashboard() put into one Thymeleaf model.
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Long deptId = authUtil.currentDepartmentId();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", UserResponse.from(authUtil.currentUser()));
        data.put("pendingExpenses", expenseService.getDepartmentExpenses(deptId));
        data.put("allDeptExpenses", expenseService.getAllDepartmentExpenses(deptId));
        data.put("team", userService.getEmployeesByDepartment(deptId));
        data.put("pendingCount", expenseService.getDeptStatusCount(deptId, "PENDING"));
        data.put("approvedCount", expenseService.getDeptStatusCount(deptId, "MANAGER_APPROVED"));
        data.put("rejectedCount", expenseService.getDeptStatusCount(deptId, "REJECTED"));
        data.put("pendingLimitRequests", limitRequestService.getPendingForDepartment(deptId));
        data.put("categoryChart", expenseService.getDeptCategorySummary(deptId));
        data.put("monthlyChart", expenseService.getDeptMonthlySummary(deptId));
        return ResponseEntity.ok(data);
    }
}
