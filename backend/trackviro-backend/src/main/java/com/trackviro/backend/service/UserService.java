package com.trackviro.backend.service;

import com.trackviro.backend.dto.user.UserCreateRequest;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.model.User;

import java.util.List;

/** Ported from com.example.demo.service.UserService, adapted to DTOs. */
public interface UserService {

    /**
     * Pre-dates JWT-based authentication, which arrives in a later
     * step. Spring Security's own DaoAuthenticationProvider +
     * UserDetailsService will be the real authentication path once
     * built. This method is not called by anything in this project —
     * it is ported only because Step 4 preserves existing business
     * logic rather than deleting it. Do NOT wire this into a
     * controller: it compares passwords in plaintext
     * (user.getPassword().equals(password)), a known issue already
     * documented in the migration checklist as dead/insecure code in
     * the old app, unlike the BCrypt-based real login that will
     * replace it.
     */
    User login(String email, String password);

    UserResponse registerUser(UserCreateRequest request);

    List<UserResponse> getEmployees();
    List<UserResponse> getManagersByDepartment(Long deptId);
    List<UserResponse> getEmployeesByDepartment(Long deptId);
    List<UserResponse> getAllActiveUsers();
}
