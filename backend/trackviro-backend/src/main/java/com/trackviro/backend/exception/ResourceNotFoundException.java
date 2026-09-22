package com.trackviro.backend.exception;

/**
 * Maps to HTTP 404. Replaces the old services' pattern of calling
 * repository.findById(id).get() and letting a raw
 * NoSuchElementException reach the client as a stack trace.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id " + id);
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
