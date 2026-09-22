package com.trackviro.backend.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Matches ProfileController's old "/changePassword" form, which required
 * currentPassword + newPassword + confirmPassword and rejected the
 * request server-side if newPassword != confirmPassword. That check is
 * preserved here via @AssertTrue instead of the controller doing it,
 * since controllers don't exist yet in this step.
 */
public record PasswordChangeRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 4, message = "Password must be at least 4 characters")
        String newPassword,

        @NotBlank(message = "Please confirm the new password")
        String confirmPassword
) {
    @AssertTrue(message = "New passwords do not match.")
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
