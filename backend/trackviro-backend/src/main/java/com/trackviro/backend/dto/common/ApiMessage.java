package com.trackviro.backend.dto.common;

/** Generic success envelope for endpoints with nothing else to return
 *  (matches the old app's RedirectAttributes flash-message pattern,
 *  e.g. ProfileServiceImpl's SUCCESS results). */
public record ApiMessage(String message) {}
