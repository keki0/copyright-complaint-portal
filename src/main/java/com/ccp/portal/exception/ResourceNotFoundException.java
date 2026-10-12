package com.ccp.portal.exception;

/**
 * Thrown when a resource does not exist OR the current user is not allowed
 * to know that it exists (e.g. another complainant's complaint). Returning
 * "not found" instead of "forbidden" prevents complaint-ID enumeration.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
