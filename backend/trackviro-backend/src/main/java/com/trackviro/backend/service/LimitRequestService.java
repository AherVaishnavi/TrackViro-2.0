package com.trackviro.backend.service;

import com.trackviro.backend.dto.limitrequest.FinanceApproveRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestCreateRequest;
import com.trackviro.backend.dto.limitrequest.LimitRequestResponse;

import java.util.List;

/** Ported from com.example.demo.service.LimitRequestService, adapted to DTOs. */
public interface LimitRequestService {

    // Employee
    LimitRequestResponse submitRequest(Long employeeId, LimitRequestCreateRequest request);
    List<LimitRequestResponse> getEmployeeRequests(Long employeeId);

    // Manager — actingManagerDeptId checked against the request's employee's department
    List<LimitRequestResponse> getPendingForDepartment(Long deptId);
    LimitRequestResponse approveByManager(Long requestId, Long actingManagerDeptId);
    LimitRequestResponse rejectByManager(Long requestId, Long actingManagerDeptId, String reason);

    // Finance — global
    List<LimitRequestResponse> getManagerApprovedRequests();
    LimitRequestResponse approveByFinance(Long requestId, FinanceApproveRequest request);
    LimitRequestResponse rejectByFinance(Long requestId, String reason);
}
