package com.trackviro.backend.controller;

import com.trackviro.backend.dto.common.ApiMessage;
import com.trackviro.backend.dto.user.OtpPasswordChangeRequest;
import com.trackviro.backend.dto.user.PasswordChangeRequest;
import com.trackviro.backend.dto.user.ProfileUpdateRequest;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.exception.BusinessRuleException;
import com.trackviro.backend.security.AuthUtil;
import com.trackviro.backend.service.ProfileService;
import com.trackviro.backend.storage.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Replaces com.example.demo.controller.ProfileController's
 * profile/password endpoints. forgotPassword/** was already moved to
 * AuthController (Step 6, this same step) since it must be reachable
 * while logged out — everything remaining here genuinely requires an
 * authenticated user, which matches how SecurityConfig (Step 5)
 * treats "/api/profile/**": authenticated(), any of the three roles.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthUtil authUtil;
    private final FileStorageService fileStorageService;

    public ProfileController(ProfileService profileService,
                             AuthUtil authUtil,
                             FileStorageService fileStorageService) {
        this.profileService = profileService;
        this.authUtil = authUtil;
        this.fileStorageService = fileStorageService;
    }

    /**
     * multipart/form-data with two parts:
     *   "profile"        — ProfileUpdateRequest fields (name, phone), as JSON
     *   "profilePicFile" — optional image file
     */
    @PutMapping(consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestPart("profile") ProfileUpdateRequest request,
            @RequestPart(value = "profilePicFile", required = false) MultipartFile profilePicFile) {

        String storedFileName = fileStorageService.storeProfilePicture(profilePicFile);
        UserResponse response = profileService.updateProfile(
                authUtil.currentUserId(), request, storedFileName);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<ApiMessage> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        // Throws BusinessRuleException("WRONG_PASSWORD", ...) on mismatch.
        profileService.changePasswordWithCurrent(authUtil.currentUserId(), request);
        return ResponseEntity.ok(new ApiMessage("Password changed."));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiMessage> sendOtp() {
        String result = profileService.sendOtp(authUtil.currentUserId());
        if ("FAILED".equals(result)) {
            throw new BusinessRuleException("MAIL_FAILED",
                    "Could not send the OTP email. Please try again shortly.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        return ResponseEntity.ok(new ApiMessage("OTP sent. It expires in 10 minutes."));
    }

    @PutMapping("/password/otp")
    public ResponseEntity<ApiMessage> changePasswordWithOtp(
            @Valid @RequestBody OtpPasswordChangeRequest request) {
        // Throws BusinessRuleException for INVALID_OTP / EXPIRED_OTP.
        profileService.verifyOtpAndChangePassword(authUtil.currentUserId(), request);
        return ResponseEntity.ok(new ApiMessage("Password changed."));
    }
}
