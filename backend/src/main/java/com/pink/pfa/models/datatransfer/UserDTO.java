package com.pink.pfa.models.datatransfer;

import com.pink.pfa.models.User;

// Client-facing representation of user information
// Nothing special, but you can easily imagine the usefulness once passwords and sensitive information starts becoming a factor
public record UserDTO(
        Integer id,
        String name,
        String email
) {
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getUser_id(),
            user.getName(),
            user.getEmail()
        );
    }
}
