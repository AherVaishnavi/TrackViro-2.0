package com.trackviro.backend.dto.auth;

import jakarta.validation.constraints.*;

/**
 * Matches ProfileController's old "/forgotPassword/reset" form
 * (email, otp, newPassword, confirmPassword) — the same
 * newPassword == confirmPassword check the old controller did is
 * preserved here.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "OTP is required")
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        String otp,

        @NotBlank(message = "New password is required")
        @Size(min = 4, message = "Password must be at least 4 characters")
        String newPassword,

        @NotBlank(message = "Please confirm the new password")
        String confirmPassword
) {
    @AssertTrue(message = "Passwords do not match.")
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
