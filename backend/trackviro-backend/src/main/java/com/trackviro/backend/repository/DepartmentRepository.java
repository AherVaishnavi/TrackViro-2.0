package com.trackviro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trackviro.backend.model.Department;

/** Ported unchanged from com.example.demo.repository.DepartmentRepository. */
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
