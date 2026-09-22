package com.trackviro.backend.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Matches ProfileController's old "/update" form fields exactly
 * (name, phone). profilePicFile is handled as a separate multipart
 * part by the controller in a later step, not part of this JSON body.
 */
public record ProfileUpdateRequest(
        @NotBlank(message = "Name is required")
        String name,

        String phone
) {}
