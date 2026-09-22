package com.trackviro.backend.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Matches ProfileController's old "/verifyOtpAndChange" form
 * (logged-in user changing password via OTP instead of current password).
 * Same confirmPassword check preserved as PasswordChangeRequest.
 */
public record OtpPasswordChangeRequest(
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
