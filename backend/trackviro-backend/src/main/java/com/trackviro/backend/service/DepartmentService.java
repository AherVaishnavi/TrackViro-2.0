package com.trackviro.backend.service;

import com.trackviro.backend.dto.department.DepartmentRequest;
import com.trackviro.backend.dto.department.DepartmentResponse;

import java.util.List;

/** Ported from com.example.demo.service.DepartmentService, adapted to DTOs.
 *  create + list only, matching the old app's actual scope. */
public interface DepartmentService {
    DepartmentResponse save(DepartmentRequest request);
    List<DepartmentResponse> getAll();
}
