package com.trackviro.backend.controller;

import com.trackviro.backend.dto.user.UserCreateRequest;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces the user-related endpoints of
 * com.example.demo.controller.FinanceController
 * ("/finance/user", "/finance/user/save"). Create + list only,
 * matching the old app's scope. registerUser (Step 4) already
 * BCrypt-encodes the password and preserves the auto-assign-
 * department-manager logic — this controller only supplies the
 * request/response translation.
 */
@RestController
@RequestMapping("/api/finance/users")
@Tag(name = "Finance", description = "Requires role FINANCE. Global (unrestricted) final approval, reimbursement, analytics, dashboard.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAllActiveUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(201).body(userService.registerUser(request));
    }
}
