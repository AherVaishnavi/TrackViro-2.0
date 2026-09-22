package com.trackviro.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.trackviro.backend.model.LimitRequest;
import com.trackviro.backend.model.User;

/** Ported unchanged from com.example.demo.repository.LimitRequestRepository. */
public interface LimitRequestRepository extends JpaRepository<LimitRequest, Long> {

    // Employee's own requests
    List<LimitRequest> findByEmployeeOrderByCreatedAtDesc(User employee);

    // Manager sees pending requests from their department
    List<LimitRequest> findByEmployeeDepartmentIdAndStatus(Long deptId, String status);

    // Finance sees manager-approved requests
    List<LimitRequest> findByStatus(String status);

    // Check if employee already has a pending request
    boolean existsByEmployeeAndStatus(User employee, String status);
}
