package com.trackviro.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.trackviro.backend.model.User;

/** Ported unchanged from com.example.demo.repository.UserRepository. */
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndIsActiveTrue(String email);

    List<User> findByRoleAndIsActiveTrue(String role);

    List<User> findByDepartmentIdAndRoleAndIsActiveTrue(Long deptId, String role);

    List<User> findByIsActiveTrue();
}
