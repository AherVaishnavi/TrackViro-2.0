package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.department.DepartmentRequest;
import com.trackviro.backend.dto.department.DepartmentResponse;
import com.trackviro.backend.model.Department;
import com.trackviro.backend.repository.DepartmentRepository;
import com.trackviro.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Ported from com.example.demo.service.impl.DepartmentServiceImpl,
 * adapted to DTOs. The old department.html form only ever submitted
 * "name" — no manager picker existed in the UI — so DepartmentRequest
 * has no manager field either, matching the old app's actual scope
 * exactly rather than adding new functionality.
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired private DepartmentRepository departmentRepository;

    @Override
    public DepartmentResponse save(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.name());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Override
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream().map(DepartmentResponse::from).toList();
    }
}
