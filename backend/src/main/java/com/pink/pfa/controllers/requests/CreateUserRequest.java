package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * Immutable request model used for user registration.
 * <p>
 * This record represents the payload expected when a client submits
 * a request to create a new user account (e.g., POST /api/users/register).
 *
 * Purpose:
 * <ul>
 *   <li>Encapsulate user-provided registration data.</li>
 *   <li>Decouple API request models from persistence entities.</li>
 *   <li>Ensure immutability of incoming request data.</li>
 * </ul>
 *
 * This object is typically populated automatically by Spring via
 * {@code @RequestBody} in the controller layer.
 *
 * @param name     display name of the user
 * @param email    email address used as login identifier
 * @param password raw password provided during registration (will be hashed before storage)
 */
public record CreateUserRequest(

        @NotBlank
        String name, 

        @Email
        @NotBlank
        String email,

        @Size(min = 8)
        String password
) {}
