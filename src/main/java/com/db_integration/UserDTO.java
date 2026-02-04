package com.db_integration;

// Client-facing representation of user information
// Nothing special, but you can easily imagine the usefulness once passwords and sensitive information starts becoming a factor
public record UserDTO(
        Long id,
        String fullname,
        String email
) {
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getUser_id(),
            String.format(user.getName()),
            user.getEmail()
        );
    }
}