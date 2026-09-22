package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.auth.ResetPasswordRequest;
import com.trackviro.backend.dto.user.OtpPasswordChangeRequest;
import com.trackviro.backend.dto.user.PasswordChangeRequest;
import com.trackviro.backend.dto.user.ProfileUpdateRequest;
import com.trackviro.backend.dto.user.UserResponse;
import com.trackviro.backend.exception.BusinessRuleException;
import com.trackviro.backend.exception.ResourceNotFoundException;
import com.trackviro.backend.model.User;
import com.trackviro.backend.repository.UserRepository;
import com.trackviro.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Ported from com.example.demo.service.impl.ProfileServiceImpl.
 *
 * OTP generation (6-digit, java.util.Random, 10-minute expiry) and the
 * email text are preserved exactly — not hardened in this step.
 * SecureRandom / attempt-limiting / cooldowns were explicitly filed
 * as "can be improved later" in the migration checklist, not part of
 * this instruction, so they're untouched here.
 *
 * REQUIRES two beans this project does not have yet:
 *  - PasswordEncoder — provided by the new
 *    com.trackviro.backend.config.PasswordEncoderConfig (see the
 *    Step 4 summary for why this had to be added).
 *  - JavaMailSender — Spring Boot only auto-configures this once
 *    spring.mail.host is set in application.properties. Without it,
 *    this bean fails to construct and the whole application context
 *    fails to start (see the Step 4 summary for the exact properties
 *    needed before you run this).
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JavaMailSender mailSender;

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request, String profilePicFileName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setName(request.name());
        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
        }
        if (profilePicFileName != null) {
            user.setProfilePic(profilePicFileName);
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePasswordWithCurrent(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessRuleException("WRONG_PASSWORD",
                    "Current password is incorrect.", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public String sendOtp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return generateAndSendOtp(user);
    }

    @Override
    public String sendOtpByEmail(String email) {
        User user = userRepository.findByEmailAndIsActiveTrue(email);
        if (user == null) {
            // Preserved exactly from the old app: a plain String
            // return, not an exception — the old ProfileController
            // decided what to show the user with this code. That
            // decision point moves to the controller in a later step.
            return "USER_NOT_FOUND";
        }
        return generateAndSendOtp(user);
    }

    @Override
    @Transactional
    public void verifyOtpAndChangePassword(Long userId, OtpPasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        validateOtpOrThrow(user, request.otp());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        clearOtp(user);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPasswordWithOtp(ResetPasswordRequest request) {
        User user = userRepository.findByEmailAndIsActiveTrue(request.email());
        if (user == null) {
            // Preserved exactly from the old app's own behaviour: a
            // distinct USER_NOT_FOUND code, not masked as an invalid
            // OTP. The old ProfileServiceImpl returned this same value
            // itself, distinguishable from INVALID_OTP. Whether to
            // mask this later — so this endpoint can't be used to
            // enumerate registered emails — is a deliberate security
            // decision for a later hardening step, not made silently
            // here.
            throw new BusinessRuleException("USER_NOT_FOUND",
                    "No account found with that email.", HttpStatus.BAD_REQUEST);
        }

        validateOtpOrThrow(user, request.otp());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        clearOtp(user);
        userRepository.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────

    private String generateAndSendOtp(User user) {
        try {
            String otp = String.valueOf(100000 + new Random().nextInt(900000)); // 6-digit
            user.setOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setSubject("TrackViro — Your OTP Code");
            msg.setText(
                "Hello " + user.getName() + ",\n\n" +
                "Your OTP for password change is: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "— TrackViro Team"
            );
            mailSender.send(msg);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED";
        }
    }

    private void validateOtpOrThrow(User user, String otp) {
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new BusinessRuleException("INVALID_OTP",
                    "That OTP is not correct.", HttpStatus.BAD_REQUEST);
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new BusinessRuleException("EXPIRED_OTP",
                    "That OTP has expired. Request a new one.", HttpStatus.BAD_REQUEST);
        }
    }

    private void clearOtp(User user) {
        user.setOtp(null);
        user.setOtpExpiry(null);
    }
}
