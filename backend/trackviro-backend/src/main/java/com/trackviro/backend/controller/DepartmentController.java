package com.trackviro.backend.controller;

import com.trackviro.backend.dto.department.DepartmentRequest;
import com.trackviro.backend.dto.department.DepartmentResponse;
import com.trackviro.backend.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces the department-related endpoints of
 * com.example.demo.controller.FinanceController
 * ("/finance/department", "/finance/department/save"). Create + list
 * only — the old department.html form never had a manager picker, so
 * DepartmentRequest (Step 3) doesn't either.
 */
@RestController
@RequestMapping("/api/finance/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentService.getAll());
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(201).body(departmentService.save(request));
    }
}
