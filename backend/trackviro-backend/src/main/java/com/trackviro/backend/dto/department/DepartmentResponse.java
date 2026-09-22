package com.trackviro.backend.dto.department;

import com.trackviro.backend.model.Department;

public record DepartmentResponse(Long id, String name, String managerName) {
    public static DepartmentResponse from(Department d) {
        if (d == null) return null;
        return new DepartmentResponse(
                d.getId(),
                d.getName(),
                d.getManager() != null ? d.getManager().getName() : null
        );
    }
}
