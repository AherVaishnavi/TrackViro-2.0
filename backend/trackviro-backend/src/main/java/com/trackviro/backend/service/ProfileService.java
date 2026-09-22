package com.trackviro.backend.service;

import com.trackviro.backend.dto.auth.ResetPasswordRequest;
import com.trackviro.backend.dto.user.OtpPasswordChangeRequest;
import com.trackviro.backend.dto.user.PasswordChangeRequest;
import com.trackviro.backend.dto.user.ProfileUpdateRequest;
import com.trackviro.backend.dto.user.UserResponse;

/**
 * Ported from com.example.demo.service.ProfileService, adapted to
 * DTOs. The old interface returned String result codes for every
 * method (SUCCESS/FAILED/WRONG_PASSWORD/INVALID_OTP/etc.). Here, codes
 * that signal a rule violation (wrong password, bad/expired OTP) are
 * thrown as BusinessRuleException instead, so GlobalExceptionHandler
 * turns them into the right HTTP status automatically. sendOtp/
 * sendOtpByEmail keep a plain String return, because "FAILED" there
 * means a mail-delivery problem, not a business rule violation — the
 * same distinction the old app's design already implied.
 */
public interface ProfileService {

    UserResponse updateProfile(Long userId, ProfileUpdateRequest request, String profilePicFileName);

    /** Throws BusinessRuleException("WRONG_PASSWORD", ...) on mismatch. */
    void changePasswordWithCurrent(Long userId, PasswordChangeRequest request);

    String sendOtp(Long userId);
    String sendOtpByEmail(String email);

    /** Throws BusinessRuleException for INVALID_OTP / EXPIRED_OTP. */
    void verifyOtpAndChangePassword(Long userId, OtpPasswordChangeRequest request);

    /** Throws BusinessRuleException for USER_NOT_FOUND / INVALID_OTP / EXPIRED_OTP. */
    void resetPasswordWithOtp(ResetPasswordRequest request);
}
