package com.trackviro.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Central error handler for the whole REST API.
 *
 * Scoped deliberately to what exists as of Step 3: request validation,
 * the two custom business exceptions, and a catch-all. It does NOT
 * handle Spring Security exceptions (BadCredentialsException,
 * AccessDeniedException, etc.) yet, because no security/JWT layer
 * exists in the project at this point (Step 3 requirement #11) — those
 * handlers are added when that layer is built, not before, so nothing
 * here silently assumes a dependency that isn't there.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400: @Valid failures on request DTOs ─────────────────────
    // Fires for every DTO in dto/**, including the confirmPassword
    // @AssertTrue checks on PasswordChangeRequest, OtpPasswordChangeRequest,
    // and ResetPasswordRequest.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ErrorResponse.of(
                400, "VALIDATION_FAILED", "Please correct the highlighted fields.", fields));
    }

    // ── Business rule codes: DUPLICATE, LIMIT_EXCEEDED, ALREADY_PENDING,
    //    WRONG_PASSWORD, INVALID_OTP, EXPIRED_OTP, etc. (see
    //    BusinessRuleException's Javadoc for the full mapping this
    //    exists to carry once services are ported) ──────────────
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity.status(ex.getStatus()).body(
                ErrorResponse.of(ex.getStatus().value(), ex.getCode(), ex.getMessage()));
    }

    // ── 404 ────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(404, "NOT_FOUND", ex.getMessage()));
    }

    // ── 403: object-level authorization (the gap the old app had —
    //    see AccessDeniedForResourceException's Javadoc) ─────────
    @ExceptionHandler(AccessDeniedForResourceException.class)
    public ResponseEntity<ErrorResponse> handleOwnership(AccessDeniedForResourceException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.of(403, "FORBIDDEN", ex.getMessage()));
    }

    // ── 500: catch-all. Never lets a raw stack trace reach the
    //    client — this is the direct replacement for the old
    //    findById(id).get() pattern's default behaviour. ─────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        ex.printStackTrace(); // keep it in the server log during development
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.of(500, "INTERNAL_ERROR", "Something went wrong. Please try again."));
    }
}
