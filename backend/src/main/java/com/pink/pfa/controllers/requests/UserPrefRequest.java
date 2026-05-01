package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Immutable request model used for user preference creation
 * <p>
 * This record represents the payload expected when a client submits
 * a request to create a new user preference object for compatibility filtering
 *
 * Purpose:
 * <ul>
 *   <li>Encapsulate user-provided filtering data.</li>
 *   <li>Decouple API request models from persistence entities.</li>
 *   <li>Ensure immutability of incoming request data.</li>
 * </ul>
 *
 * This object is typically populated automatically by Spring via
 * {@code @RequestBody} in the controller layer.
 *
 * @param pref     specific preference type
 * @param value    value the preference holds
 */
public record UserPrefRequest(
    @NotBlank
    String pref, 

    @NotBlank
    String value
) {}
