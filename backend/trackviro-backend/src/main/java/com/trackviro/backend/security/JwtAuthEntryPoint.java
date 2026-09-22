package com.trackviro.backend.security;

import tools.jackson.databind.ObjectMapper;
import com.trackviro.backend.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Without this, Spring Security's default behaviour on a missing/bad
 * token is to redirect toward a login page that doesn't exist in a
 * REST API — which a React client can't meaningfully follow. This
 * returns a clean JSON 401 instead, reusing the same ErrorResponse
 * shape GlobalExceptionHandler already uses (built in Step 3), so
 * every error the API returns — auth failures included — has one
 * consistent JSON body.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public JwtAuthEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), ErrorResponse.of(
                401, "UNAUTHORIZED", "Authentication required. Please log in again."));
    }
}
