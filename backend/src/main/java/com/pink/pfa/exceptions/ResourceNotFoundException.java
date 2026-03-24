package com.pink.pfa.exceptions;

/**
 * Thrown when a requested resource cannot be found in the system.
 * <p>
 * This is an unchecked exception intended to be thrown from service layer
 * methods and caught at the controller layer to return an appropriate
 * HTTP 404 (Not Found) response.
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception identifying the missing resource by type and ID.
     *
     * @param resourceType the name of the resource type that was not found (e.g. {@code "User"})
     * @param id           the ID that was looked up
     */
    public ResourceNotFoundException(String resourceType, Object id) {
        super(resourceType + " not found with id: " + id);
    }
}
