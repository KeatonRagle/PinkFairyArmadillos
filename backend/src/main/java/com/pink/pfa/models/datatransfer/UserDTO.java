package com.pink.pfa.models.datatransfer;

import com.pink.pfa.models.User;


/**
 * Client-facing Data Transfer Object (DTO) representing a safe subset
 * of user information.
 * <p>
 * This record is used to expose user data to API consumers without
 * leaking sensitive fields such as passwords, roles, or internal metadata.
 *
 * Purpose:
 * <ul>
 *   <li>Decouple the persistence layer ({@link User}) from the API response layer.</li>
 *   <li>Prevent accidental exposure of sensitive fields (e.g., hashed passwords).</li>
 *   <li>Provide a stable contract for frontend clients.</li>
 * </ul>
 *
 * By using a DTO instead of returning the entity directly, the application
 * maintains stronger security and cleaner architectural boundaries.
 *
 * @param id unique identifier of the user
 * @param name display name of the user
 * @param email email address used as login identifier
 */
public record UserDTO(
        Integer id,
        String name,
        String email
) {


    /**
     * Converts a {@link User} entity into a {@link UserDTO}.
     * <p>
     * This method extracts only the fields that are safe to expose
     * to clients.
     *
     * @param user persisted user entity
     * @return corresponding {@link UserDTO}
     */
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getUser_id(),
            user.getName(),
            user.getEmail()
        );
    }
}
