package com.trackviro.backend.dto.auth;

import com.trackviro.backend.dto.user.UserResponse;

/**
 * token/tokenType/expiresInMs are placeholders for the JWT step —
 * not implemented yet per Step 3 scope (#11: no JWT/security).
 * The shape exists now so later steps don't have to change the
 * response contract, only populate these fields for real.
 */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {}
