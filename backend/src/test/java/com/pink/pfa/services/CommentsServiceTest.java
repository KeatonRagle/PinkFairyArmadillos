package com.pink.pfa.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.config.TestcontainersConfiguration;
import com.pink.pfa.context.PfaBase;
import com.pink.pfa.models.Comments;


/**
 * Integration test suite for {@link JWTService}, verifying the correctness and security
 * properties of the application's JWT-based authentication mechanism.
 *
 * <p>This test class spins up a full Spring application context backed by a Testcontainers-managed
 * MySQL instance (via {@link TestcontainersConfiguration}) and seeds it with known users
 * (via {@link TestDataConfig}). Tests operate against real Spring beans rather than mocks,
 * ensuring that JWT generation, parsing, and validation behave correctly end-to-end.
 *
 * <p>The following behavioral categories are covered:
 * <ul>
 *   <li><b>Token generation:</b> Verifies that generated tokens are non-null and immediately valid.</li>
 *   <li><b>Expiration:</b> Confirms that token expiration is set to approximately 30 minutes from issuance.</li>
 *   <li><b>Email extraction:</b> Validates that the subject claim can be correctly retrieved from
 *       both raw tokens and {@code Authorization} header strings.</li>
 *   <li><b>Validation:</b> Ensures tokens are rejected when presented for the wrong user or when
 *       the signature has been tampered with.</li>
 *   <li><b>Key isolation:</b> Documents and enforces the behavior that tokens issued by one
 *       {@link JWTService} instance are rejected by a separate instance with a different signing key,
 *       reflecting what happens across application restarts.</li>
 * </ul>
 *
 * <p>Seeded test users (sourced from {@link TestDataConfig}):
 * <ul>
 *   <li>{@code austin@pfa.com}</li>
 *   <li>{@code dylan@pfa.com}</li>
 *   <li>{@code keaton@pfa.com}</li>
 * </ul>
 *
 * <p>All email constants are lowercase to account for the normalization applied by
 * {@link CustomUserDetailsService#loadUserByUsername(String)}.
 */
class CommentsServiceTest extends PfaBase {

    /**
     * Constructs the test instance with the required Spring-managed services.
     *
     * @param jwtService         the JWT service under test
     * @param userDetailsService used to load seeded test users for validation assertions
     */


    /**
     * Verifies that a generated token is non-null and passes validation for the correct user.
     */
    @Test
    void shouldGenerateValidComment() {
        Comments comment = getRandComment();
        assertNotNull(comment);
    }
}
