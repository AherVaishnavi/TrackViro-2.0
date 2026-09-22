package com.trackviro.backend.controller;

import com.trackviro.backend.dto.common.ApiMessage;
import com.trackviro.backend.dto.common.ChartData;
import com.trackviro.backend.dto.expense.ExpenseRequest;
import com.trackviro.backend.dto.expense.ExpenseResponse;
import com.trackviro.backend.dto.limitrequest.LimitRequestCreateRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestResponse;
import com.trackviro.backend.dto.user.UserResponse;
import java.util.List;
import com.trackviro.backend.security.AuthUtil;
import com.trackviro.backend.service.ExpenseService;
import com.trackviro.backend.service.LimitRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.trackviro.backend.storage.FileStorageService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Replaces com.example.demo.controller.EmployeeController (dashboard,
 * submit_expense form, save) and the employee-side of
 * com.example.demo.controller.LimitRequestController
 * ("/employee/limitRequest"). All endpoints here live under
 * "/api/employee/**", which SecurityConfig (Step 5) already restricts
 * to hasRole("EMPLOYEE") — no per-method authorization annotation is
 * needed on top of that.
 *
 * employeeId is read from AuthUtil (the JWT), never trusted from the
 * request body — the old app read it off session.getAttribute("loggedUser")
 * for the same reason: a caller must act as themselves, not whoever
 * they claim in a form field.
 */
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final ExpenseService expenseService;
    private final LimitRequestService limitRequestService;
    private final AuthUtil authUtil;
    private final FileStorageService fileStorageService;

    public EmployeeController(ExpenseService expenseService,
                              LimitRequestService limitRequestService,
                              AuthUtil authUtil,
                              FileStorageService fileStorageService) {
        this.expenseService = expenseService;
        this.limitRequestService = limitRequestService;
        this.authUtil = authUtil;
        this.fileStorageService = fileStorageService;
    }

    // ── Expenses ──────────────────────────────────────────────

    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponse>> getExpenses() {
        return ResponseEntity.ok(expenseService.getEmployeeExpenses(authUtil.currentUserId()));
    }

    /**
     * multipart/form-data with two parts:
     *   "expense"  — the ExpenseRequest fields, as a JSON part
     *   "billFile" — optional file part (JPEG/PNG/PDF, 5MB max)
     * Matches the old submit_expense.html form's fields plus its
     * optional bill upload, combined into one REST call instead of a
     * page load + form POST.
     */
    @PostMapping(value = "/expenses", consumes = "multipart/form-data")
    public ResponseEntity<ExpenseResponse> submitExpense(
            @Valid @RequestPart("expense") ExpenseRequest request,
            @RequestPart(value = "billFile", required = false) MultipartFile billFile) {

        String billPath = fileStorageService.storeBill(billFile);

        // Throws BusinessRuleException("DUPLICATE", ...) or
        // ("LIMIT_EXCEEDED", ...) — handled by GlobalExceptionHandler.
        ExpenseResponse response = expenseService.submitExpense(
                authUtil.currentUserId(), request, billPath);

        return ResponseEntity.status(201).body(response);
    }

    // ── Analytics ─────────────────────────────────────────────

    @GetMapping("/analytics/category")
    public ResponseEntity<ChartData> getCategoryChart() {
        return ResponseEntity.ok(expenseService.getEmpCategorySummary(authUtil.currentUserId()));
    }

    @GetMapping("/analytics/monthly")
    public ResponseEntity<ChartData> getMonthlyChart() {
        return ResponseEntity.ok(expenseService.getEmpMonthlySummary(authUtil.currentUserId()));
    }

    // ── Limit requests ────────────────────────────────────────

    @PostMapping("/limit-requests")
    public ResponseEntity<LimitRequestResponse> submitLimitRequest(
            @Valid @RequestBody LimitRequestCreateRequest request) {

        // Throws BusinessRuleException("ALREADY_PENDING", ...) on a
        // second pending request — handled by GlobalExceptionHandler.
        LimitRequestResponse response =
                limitRequestService.submitRequest(authUtil.currentUserId(), request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/limit-requests")
    public ResponseEntity<List<LimitRequestResponse>> getLimitRequests() {
        return ResponseEntity.ok(limitRequestService.getEmployeeRequests(authUtil.currentUserId()));
    }

    // ── Dashboard ─────────────────────────────────────────────
    // Bundles what the old EmployeeController.dashboard() put into a
    // single Thymeleaf model, so a React dashboard page can render
    // with one call instead of six. Returned as a plain Map, not a
    // new named DTO class — Step 6 asks to use the DTOs already
    // created, and the values inside are all existing DTOs; only the
    // envelope is a Map.
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Long employeeId = authUtil.currentUserId();
        UserResponse user = UserResponse.from(authUtil.currentUser());

        Double monthlyTotal = expenseService.getMonthlyTotal(employeeId);
        if (monthlyTotal == null) monthlyTotal = 0.0;
        Double remainingBalance = (user.monthlyLimit() == null ? 0.0 : user.monthlyLimit()) - monthlyTotal;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", user);
        data.put("expenses", expenseService.getEmployeeExpenses(employeeId));
        data.put("monthlyTotal", monthlyTotal);
        data.put("remainingBalance", remainingBalance);
        data.put("pendingCount", expenseService.getStatusCount(employeeId, "PENDING"));
        data.put("managerApprovedCount", expenseService.getStatusCount(employeeId, "MANAGER_APPROVED"));
        data.put("financeApprovedCount", expenseService.getStatusCount(employeeId, "FINANCE_APPROVED"));
        data.put("reimbursedCount", expenseService.getStatusCount(employeeId, "REIMBURSED"));
        data.put("approvedAmount", expenseService.getApprovedAmount(employeeId));
        data.put("reimbursedAmount", expenseService.getReimbursedAmount(employeeId));
        data.put("limitRequests", limitRequestService.getEmployeeRequests(employeeId));
        data.put("categoryChart", expenseService.getEmpCategorySummary(employeeId));
        data.put("monthlyChart", expenseService.getEmpMonthlySummary(employeeId));
        return ResponseEntity.ok(data);
    }
}
