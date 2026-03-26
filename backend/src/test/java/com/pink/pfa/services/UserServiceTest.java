package com.pink.pfa.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.models.User;
import com.pink.pfa.models.datatransfer.UserDTO;

/**
 * Authentication Security Tests for {@link UserService}.
 * <p>
 * Covers:
 * <ul>
 *   <li>Invalid login attempt (wrong password)</li>
 *   <li>Login with a nonexistent user account</li>
 *   <li>Duplicate account creation attempt</li>
 *   <li>Password storage — verifying plaintext is never persisted</li>
 * </ul>
 */
class UserServiceTest extends PfaBase {


    // -------------------------------------------------------------------------
    // Authentication Security Tests
    // -------------------------------------------------------------------------

    /**
     * Invalid Login Attempt
     * <p>
     * Attempts login with a valid email but an incorrect password.
     * Expects an authentication exception — no indication of which field failed.
     */
    @Test
    void invalidLoginAttempt_WrongPassword_ShouldThrowAuthException() {
        UserRequest badPassword = new UserRequest("Austin", "austin@pfa.com", "wrongpassword123");

        assertThrows(AuthenticationException.class, () -> userService.verify(badPassword));
    }


    /**
     * Nonexistent User Login
     * <p>
     * Attempts login with an email that does not exist in the database.
     * Expects an authentication exception with no user data leaked.
     */
    @Test
    void nonexistentUserLogin_ShouldThrowAuthException() {
        UserRequest ghost = new UserRequest("Ghost", "ghost_does_not_exist@pfa.com", "anypassword");

        assertThrows(AuthenticationException.class, () -> userService.verify(ghost));
    }


    /**
     * Duplicate Account Creation
     * <p>
     * Attempts to register a second account using the same email as an existing user.
     * Expects an exception to be thrown and no duplicate entry created in the database.
     */
    @Test
    void duplicateAccountCreation_ShouldThrowException() {
        // austin@pfa.com is already seeded by TestDataConfig
        UserRequest duplicate = new UserRequest("Austin2", "austin@pfa.com", "differentpassword");

        assertThrows(Exception.class, () -> userService.createUser(duplicate));

        long count = userRepository.findAll()
                .stream()
                .filter(u -> u.getEmail().equals("austin@pfa.com"))
                .count();
        assertTrue(count == 1, "Should be exactly one account for this email");
    }


    /**
     * Password Storage Verification
     * <p>
     * Inspects the stored password for a newly created user.
     * Verifies the password is hashed (BCrypt) and never stored as plaintext.
     */
    @Test
    void passwordStoredAsHash_ShouldNeverBePlaintext() {
        String rawPassword = "secureTestPass99";
        userService.createUser(new UserRequest("HashUser", "hashtest_unique@pfa.com", rawPassword));

        User stored = userRepository.findByEmail("hashtest_unique@pfa.com").orElseThrow();
        String storedPassword = stored.getPassword();

        assertNotEquals(rawPassword, storedPassword, "Password must not be stored in plaintext");
        assertTrue(
            storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$"),
            "Password must be stored as a BCrypt hash"
        );
    }


    // -------------------------------------------------------------------------
    // Read Operations
    // -------------------------------------------------------------------------

    /**
     * Verifies that findAll returns all seeded users and that each DTO
     * contains only the expected safe fields — id, name, email, and role.
     * The absence of a password field is enforced by the DTO's structure itself.
     */
    @Test
    void findAll_ShouldReturnAllSeededUsers_WithSafeFieldsOnly() {
        List<UserDTO> users = userService.findAll();

        assertTrue(users.size() >= 3, "Should have at least 3 seeded users");
        users.forEach(u -> {
            assertNotNull(u.id(),    "ID should be present for: " + u.email());
            assertNotNull(u.name(),  "Name should be present for: " + u.email());
            assertNotNull(u.email(), "Email should be present for: " + u.email());
            assertNotNull(u.role(),  "Role should be present for: " + u.email());
        });
    }

        
    /**
     * Verifies that a user can be fetched by ID and the returned DTO matches
     * the expected email.
     */
    @Test
    void findById_WithValidId_ShouldReturnCorrectUser() {
        User austin = userRepository.findByEmail("austin@pfa.com").orElseThrow();
        UserDTO result = userService.findById(austin.getUserId());

        assertEquals("austin@pfa.com", result.email());
    }


    /**
     * Verifies that fetching a user with a nonexistent ID throws an exception.
     */
    @Test
    void findById_WithInvalidId_ShouldThrowException() {
        assertThrows(Exception.class, () -> userService.findById(999999));
    }


    /**
     * Verifies that a user can be fetched by email and the returned DTO is populated.
     */
    @Test
    void findByEmail_WithValidEmail_ShouldReturnUser() {
        UserDTO result = userService.findByEmail("austin@pfa.com");

        assertNotNull(result);
        assertEquals("austin@pfa.com", result.email());
    }

    /**
     * Verifies that fetching a user with a nonexistent email throws an exception.
     */
    @Test
    void findByEmail_WithNonexistentEmail_ShouldThrowException() {
        assertThrows(Exception.class,
                () -> userService.findByEmail("doesnotexist@pfa.com"));
    }


    // -------------------------------------------------------------------------
    // Role Management
    // -------------------------------------------------------------------------

    /**
     * Verifies that promoteToAdmin updates the user's role to ROLE_ADMIN
     * and that the change is persisted to the database.
     */
    @Test
    void promoteToAdmin_ShouldUpdateRoleInDatabase() {
        User keaton = userRepository.findByEmail("keaton@pfa.com").orElseThrow();
        userService.promoteToAdmin(keaton.getUserId());

        User updated = userRepository.findById(keaton.getUserId()).orElseThrow();
        assertEquals(User.Role.ROLE_ADMIN, updated.getRole());
    }


    // -------------------------------------------------------------------------
    // Input Normalization
    // -------------------------------------------------------------------------

    /**
     * Verifies that emails with mixed case and surrounding whitespace are
     * normalized to lowercase and trimmed before being stored.
     */
    @Test
    void createUser_EmailNormalization_ShouldStoreLowercase() {
        userService.createUser(new UserRequest("NormTest", "  NORMTEST@PFA.COM  ", "password123"));

        User stored = userRepository.findByEmail("normtest@pfa.com").orElseThrow();
        assertEquals("normtest@pfa.com", stored.getEmail());
    }
}
