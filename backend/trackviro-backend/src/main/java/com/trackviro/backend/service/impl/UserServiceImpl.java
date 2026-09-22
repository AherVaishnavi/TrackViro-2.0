package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.user.UserCreateRequest;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.exception.ResourceNotFoundException;
import com.trackviro.backend.model.Department;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.DepartmentRepository;
import com.trackviro.backend.repository.UserRepository;
import com.trackviro.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ported from com.example.demo.service.impl.UserServiceImpl.
 *
 * registerUser's auto-assign-department-manager logic (creating a
 * MANAGER automatically sets that department's manager_id to them) is
 * preserved exactly. login() is also ported unchanged — see its
 * Javadoc in UserService for why it exists but is not wired anywhere.
 * Read methods now return UserResponse instead of User, per Step 4's
 * DTO-adaptation instruction.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DepartmentRepository departmentRepository;

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmailAndIsActiveTrue(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {
        User user = new User();
        user.setEmployeeCode(request.employeeCode());
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setPhone(request.phone());
        user.setMonthlyLimit(request.monthlyLimit());
        user.setIsActive(true);

        if (request.departmentId() != null) {
            Department dept = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));
            user.setDepartment(dept);
        }

        User savedUser = userRepository.save(user);

        // Auto-update department's manager_id when a MANAGER is
        // created — preserved exactly from the old app.
        if ("MANAGER".equals(savedUser.getRole()) && savedUser.getDepartment() != null) {
            Department dept = departmentRepository.findById(savedUser.getDepartment().getId()).orElse(null);
            if (dept != null) {
                dept.setManager(savedUser);
                departmentRepository.save(dept);
            }
        }

        return UserResponse.from(savedUser);
    }

    @Override
    public List<UserResponse> getEmployees() {
        return userRepository.findByRoleAndIsActiveTrue("EMPLOYEE")
                .stream().map(UserResponse::from).toList();
    }

    @Override
    public List<UserResponse> getManagersByDepartment(Long deptId) {
        return userRepository.findByDepartmentIdAndRoleAndIsActiveTrue(deptId, "MANAGER")
                .stream().map(UserResponse::from).toList();
    }

    @Override
    public List<UserResponse> getEmployeesByDepartment(Long deptId) {
        return userRepository.findByDepartmentIdAndRoleAndIsActiveTrue(deptId, "EMPLOYEE")
                .stream().map(UserResponse::from).toList();
    }

    @Override
    public List<UserResponse> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue()
                .stream().map(UserResponse::from).toList();
    }
}
