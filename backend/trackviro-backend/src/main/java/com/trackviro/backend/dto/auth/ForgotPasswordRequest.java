package com.trackviro.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Matches ProfileController's old "/forgotPassword/sendOtp" (email only). */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email
) {}
