package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.limitrequest.FinanceApproveRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestCreateRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestResponse;
import com.trackviro.backend.exception.AccessDeniedForResourceException;
import com.trackviro.backend.exception.BusinessRuleException;
import com.trackviro.backend.exception.ResourceNotFoundException;
import com.trackviro.backend.model.LimitRequest;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.LimitRequestRepository;
import com.trackviro.backend.repository.UserRepository;
import com.trackviro.backend.service.LimitRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ported from com.example.demo.service.impl.LimitRequestServiceImpl.
 * Business logic unchanged: the one-pending-request rule, the
 * two-stage approval flow, and the monthlyLimit increment on finance
 * approval are all preserved exactly.
 *
 * Added beyond a straight port, per Step 4 instructions:
 *  1. Object-level authorization on approveByManager/rejectByManager —
 *     the same gap ExpenseServiceImpl had.
 *  2. @Transactional on approveByFinance specifically. This is the one
 *     method in the whole old codebase that mutates two entities (the
 *     LimitRequest AND the User's monthlyLimit) with no transaction
 *     boundary at all. That was a genuine correctness gap: a failure
 *     between the two saves could leave a request marked APPROVED
 *     without the employee's limit actually increasing.
 */
@Service
public class LimitRequestServiceImpl implements LimitRequestService {

    @Autowired private LimitRequestRepository limitRequestRepository;
    @Autowired private UserRepository userRepository;

    @Override
    @Transactional
    public LimitRequestResponse submitRequest(Long employeeId, LimitRequestCreateRequest request) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));

        boolean alreadyPending = limitRequestRepository.existsByEmployeeAndStatus(employee, "PENDING");
        if (alreadyPending) {
            throw new BusinessRuleException("ALREADY_PENDING",
                    "You already have a pending limit request.", HttpStatus.CONFLICT);
        }

        LimitRequest req = new LimitRequest();
        req.setEmployee(employee);
        req.setRequestedAmount(request.requestedAmount());
        req.setReason(request.reason());
        req.setStatus("PENDING");

        return LimitRequestResponse.from(limitRequestRepository.save(req));
    }

    @Override
    public List<LimitRequestResponse> getEmployeeRequests(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));
        return limitRequestRepository.findByEmployeeOrderByCreatedAtDesc(employee)
                .stream().map(LimitRequestResponse::from).toList();
    }

    @Override
    public List<LimitRequestResponse> getPendingForDepartment(Long deptId) {
        return limitRequestRepository.findByEmployeeDepartmentIdAndStatus(deptId, "PENDING")
                .stream().map(LimitRequestResponse::from).toList();
    }

    @Override
    @Transactional
    public LimitRequestResponse approveByManager(Long requestId, Long actingManagerDeptId) {
        LimitRequest req = loadRequest(requestId);
        assertSameDepartment(req, actingManagerDeptId);

        req.setStatus("MANAGER_APPROVED");
        return LimitRequestResponse.from(limitRequestRepository.save(req));
    }

    @Override
    @Transactional
    public LimitRequestResponse rejectByManager(Long requestId, Long actingManagerDeptId, String reason) {
        LimitRequest req = loadRequest(requestId);
        assertSameDepartment(req, actingManagerDeptId);

        req.setStatus("REJECTED");
        req.setRejectReason(reason);
        return LimitRequestResponse.from(limitRequestRepository.save(req));
    }

    @Override
    public List<LimitRequestResponse> getManagerApprovedRequests() {
        return limitRequestRepository.findByStatus("MANAGER_APPROVED")
                .stream().map(LimitRequestResponse::from).toList();
    }

    @Override
    @Transactional
    public LimitRequestResponse approveByFinance(Long requestId, FinanceApproveRequest request) {
        LimitRequest req = loadRequest(requestId);
        req.setStatus("APPROVED");
        req.setApprovedAmount(request.approvedAmount());
        LimitRequest savedReq = limitRequestRepository.save(req);

        User employee = savedReq.getEmployee();
        employee.setMonthlyLimit(employee.getMonthlyLimit() + request.approvedAmount());
        userRepository.save(employee);

        return LimitRequestResponse.from(savedReq);
    }

    @Override
    @Transactional
    public LimitRequestResponse rejectByFinance(Long requestId, String reason) {
        LimitRequest req = loadRequest(requestId);
        req.setStatus("REJECTED");
        req.setRejectReason(reason);
        return LimitRequestResponse.from(limitRequestRepository.save(req));
    }

    // ── Helpers ───────────────────────────────────────────────

    private LimitRequest loadRequest(Long id) {
        return limitRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LimitRequest", id));
    }

    private void assertSameDepartment(LimitRequest request, Long actingManagerDeptId) {
        Long reqDeptId = request.getEmployee() != null && request.getEmployee().getDepartment() != null
                ? request.getEmployee().getDepartment().getId()
                : null;

        if (actingManagerDeptId == null || !actingManagerDeptId.equals(reqDeptId)) {
            throw new AccessDeniedForResourceException(
                    "You can only act on limit requests from your own department.");
        }
    }
}
