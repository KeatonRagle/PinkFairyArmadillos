package com.pink.pfa.services;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.AuthenticationException;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.config.TestcontainersConfiguration;
import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.models.User;
import com.pink.pfa.repos.UserRepository;


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
@Import({TestcontainersConfiguration.class, TestDataConfig.class})
@SpringBootTest
class UserServiceTest {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceTest (UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }


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
}
