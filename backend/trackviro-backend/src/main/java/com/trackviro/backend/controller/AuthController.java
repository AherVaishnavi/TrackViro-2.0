package com.trackviro.backend.controller;

import com.trackviro.backend.dto.auth.ForgotPasswordRequest;
import com.trackviro.backend.dto.auth.LoginRequest;
import com.trackviro.backend.dto.auth.LoginResponse;
import com.trackviro.backend.dto.auth.ResetPasswordRequest;
import com.trackviro.backend.dto.common.ApiMessage;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.exception.BusinessRuleException;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.UserRepository;
import com.trackviro.backend.security.AuthUtil;
import com.trackviro.backend.security.CustomUserDetails;
import com.trackviro.backend.security.JwtUtil;
import com.trackviro.backend.service.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * NEW — replaces com.example.demo.controller.LoginController (which
 * returned Thymeleaf view names) and the auth-related endpoints that
 * lived inside com.example.demo.controller.ProfileController
 * (forgotPassword/sendOtp, forgotPassword/reset).
 *
 * All endpoints here are public — this whole class maps under
 * "/api/auth/**", which SecurityConfig (Step 5) already permits.
 * This satisfies "keep forgot-password endpoints public": the old
 * app's actual bug was that /profile/forgotPassword/** fell under
 * anyRequest().authenticated() and never worked while logged out.
 * Moving these here, under a path that is public by design, is the
 * direct fix.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public: login, current user, forgot-password/OTP. No token required.")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthUtil authUtil;
    private final ProfileService profileService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          AuthUtil authUtil,
                          ProfileService profileService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authUtil = authUtil;
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        // Delegates to DaoAuthenticationProvider (Step 5), which uses
        // CustomUserDetailsService + BCrypt under the hood. Throws
        // BadCredentialsException/DisabledException on failure — Spring
        // Security's own exceptions, not ones GlobalExceptionHandler
        // maps yet (see the Step 6 summary for why login failures
        // currently surface as a generic 500 rather than a clean 401).
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(principal);

        // SecurityContext isn't populated at this exact point, so the
        // user is looked up directly here — used only to build the
        // response DTO, never returned as an entity.
        User user = userRepository.findByEmailAndIsActiveTrue(principal.getUsername());

        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        "Bearer",
                        jwtUtil.getExpirationMs(),
                        UserResponse.from(user)
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(UserResponse.from(authUtil.currentUser()));
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiMessage> sendResetOtp(@Valid @RequestBody ForgotPasswordRequest request) {

        String result = profileService.sendOtpByEmail(request.email());

        // Deliberately the same response whether the email is
        // registered or not (result is USER_NOT_FOUND or SUCCESS) —
        // this endpoint must not let a caller enumerate which emails
        // exist. A genuine mail-delivery failure ("FAILED") is a real
        // infrastructure problem and IS surfaced, since there's
        // nothing sensitive to protect there.
        if ("FAILED".equals(result)) {
            throw new BusinessRuleException("MAIL_FAILED",
                    "Could not send the OTP email. Please try again shortly.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        return ResponseEntity.ok(new ApiMessage(
                "If that email is registered, an OTP has been sent. It expires in 10 minutes."));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiMessage> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // Throws BusinessRuleException for USER_NOT_FOUND / INVALID_OTP /
        // EXPIRED_OTP — handled by GlobalExceptionHandler already.
        profileService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(new ApiMessage("Password reset. You can now log in."));
    }
}
