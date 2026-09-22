package com.trackviro.backend.controller;

import com.trackviro.backend.dto.expense.RejectRequest;
import com.trackviro.backend.dto.limitrequest.FinanceApproveRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestResponse;
import com.trackviro.backend.security.AuthUtil;
import com.trackviro.backend.service.LimitRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces the manager and finance halves of
 * com.example.demo.controller.LimitRequestController — the employee
 * half ("/employee/limitRequest") lives in EmployeeController instead,
 * next to the rest of that role's endpoints.
 *
 * This single class maps to TWO different URL prefixes
 * ("/api/manager/limit-requests" and "/api/finance/limit-requests"),
 * which is intentional: SecurityConfig (Step 5) already enforces
 * hasRole("MANAGER") vs hasRole("FINANCE") purely by URL prefix, so
 * no per-method @PreAuthorize is needed here — the routing itself is
 * the authorization boundary. RejectRequest (built in Step 3 for
 * expense rejection) is reused here too, exactly as its own Javadoc
 * says it would be.
 *
 * Object-level authorization (a manager can only act on their own
 * department's requests) is enforced inside LimitRequestServiceImpl
 * (Step 4) via actingManagerDeptId — this controller's only job is to
 * supply that value from the JWT via AuthUtil, never to re-implement
 * the check.
 */
@RestController
public class LimitRequestController {

    private final LimitRequestService limitRequestService;
    private final AuthUtil authUtil;

    public LimitRequestController(LimitRequestService limitRequestService, AuthUtil authUtil) {
        this.limitRequestService = limitRequestService;
        this.authUtil = authUtil;
    }

    // ── Manager ───────────────────────────────────────────────

    @GetMapping("/api/manager/limit-requests")
    public ResponseEntity<List<LimitRequestResponse>> getPendingForMyDepartment() {
        return ResponseEntity.ok(
                limitRequestService.getPendingForDepartment(authUtil.currentDepartmentId()));
    }

    @PatchMapping("/api/manager/limit-requests/{id}/approve")
    public ResponseEntity<LimitRequestResponse> managerApprove(@PathVariable Long id) {
        // Throws AccessDeniedForResourceException (403) if the request
        // isn't from this manager's own department.
        return ResponseEntity.ok(
                limitRequestService.approveByManager(id, authUtil.currentDepartmentId()));
    }

    @PatchMapping("/api/manager/limit-requests/{id}/reject")
    public ResponseEntity<LimitRequestResponse> managerReject(
            @PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(limitRequestService.rejectByManager(
                id, authUtil.currentDepartmentId(), request.reason()));
    }

    // ── Finance ───────────────────────────────────────────────

    @GetMapping("/api/finance/limit-requests")
    public ResponseEntity<List<LimitRequestResponse>> getManagerApprovedRequests() {
        return ResponseEntity.ok(limitRequestService.getManagerApprovedRequests());
    }

    @PatchMapping("/api/finance/limit-requests/{id}/approve")
    public ResponseEntity<LimitRequestResponse> financeApprove(
            @PathVariable Long id, @Valid @RequestBody FinanceApproveRequest request) {
        return ResponseEntity.ok(limitRequestService.approveByFinance(id, request));
    }

    @PatchMapping("/api/finance/limit-requests/{id}/reject")
    public ResponseEntity<LimitRequestResponse> financeReject(
            @PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(limitRequestService.rejectByFinance(id, request.reason()));
    }
}
