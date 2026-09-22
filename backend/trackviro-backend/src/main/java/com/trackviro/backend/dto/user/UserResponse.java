package com.trackviro.backend.dto.user;

import com.trackviro.backend.model.User;
import java.time.LocalDateTime;

/**
 * The ONLY shape a User ever leaves the API in.
 *
 * Deliberately has NO password field, NO otp field, NO otpExpiry field —
 * not blanked, not @JsonIgnore'd, structurally absent from this class,
 * per Step 3 requirement #4. There is no getter here that could ever
 * be asked to serialize them.
 */
public record UserResponse(
        Long id,
        String employeeCode,
        String name,
        String email,
        String role,
        Double monthlyLimit,
        String phone,
        String profilePic,
        Boolean isActive,
        LocalDateTime createdAt,
        Long departmentId,
        String departmentName
) {
    public static UserResponse from(User u) {
        if (u == null) return null;
        return new UserResponse(
                u.getId(),
                u.getEmployeeCode(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getMonthlyLimit(),
                u.getPhone(),
                u.getProfilePic(),
                u.getIsActive(),
                u.getCreatedAt(),
                u.getDepartment() != null ? u.getDepartment().getId()   : null,
                u.getDepartment() != null ? u.getDepartment().getName() : null
        );
    }
}
