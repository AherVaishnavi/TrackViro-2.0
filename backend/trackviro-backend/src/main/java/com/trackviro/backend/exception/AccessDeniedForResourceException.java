package com.trackviro.backend.exception;

/**
 * Maps to HTTP 403. For the object-level authorization checks that
 * were MISSING in the old app (a manager could approve any
 * department's expense by changing the ID in the request) — this
 * exception exists now so the fix has somewhere to plug into once
 * the service layer is ported in a later step. Not wired to any
 * controller yet, since controllers don't exist in this step.
 */
public class AccessDeniedForResourceException extends RuntimeException {
    public AccessDeniedForResourceException(String message) {
        super(message);
    }
}
