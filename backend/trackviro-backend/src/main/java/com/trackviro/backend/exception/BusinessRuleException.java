package com.trackviro.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Carries the exact string codes the old services already return —
 * DUPLICATE, LIMIT_EXCEEDED (ExpenseServiceImpl); ALREADY_PENDING
 * (LimitRequestServiceImpl); WRONG_PASSWORD, USER_NOT_FOUND,
 * INVALID_OTP, EXPIRED_OTP, FAILED (ProfileServiceImpl) — so the
 * business logic in those services does not have to change when it's
 * ported in a later step. A future service throws this instead of
 * returning a string; the controller layer never has to translate
 * magic strings itself.
 */
public class BusinessRuleException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BusinessRuleException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
