package com.pink.pfa.exceptions;


/**
 * Thrown when a registration attempt is made with an email address that is
 * already associated with an existing user account.
 * <p>
 * This is an unchecked exception intended to be thrown from
 * {@link com.pink.pfa.services.UserService} and caught at the controller layer
 * to return an appropriate HTTP 409 (Conflict) response.
 * </p>
 */
public class UserAlreadyExistsException extends RuntimeException {


    /**
     * Constructs a new exception with a message identifying the conflicting email.
     *
     * @param email the email address that is already registered
     */
    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
    }
}
