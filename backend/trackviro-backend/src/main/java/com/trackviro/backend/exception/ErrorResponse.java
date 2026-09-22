package com.trackviro.backend.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** Shape of every error JSON body the API returns. fieldErrors is
 *  populated only for @Valid failures; null otherwise. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, code, message, null);
    }
    public static ErrorResponse of(int status, String code, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, code, message, fieldErrors);
    }
}
