package com.pink.pfa.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.config.TestcontainersConfiguration;
import com.pink.pfa.context.PfaBase;


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
class JWTServiceTest extends PfaBase {

    private final JWTService jwtService;
    private final CustomUserDetailsService userDetailsService;


    /**
     * Constructs the test instance with the required Spring-managed services.
     *
     * @param jwtService         the JWT service under test
     * @param userDetailsService used to load seeded test users for validation assertions
     */
    @Autowired
    public JWTServiceTest (JWTService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    private static final String AUSTIN_EMAIL = "austin@pfa.com";
    private static final String DYLAN_EMAIL = "dylan@pfa.com";
    private static final String KEATON_EMAIL = "keaton@pfa.com";


    /**
     * Verifies that a generated token is non-null and passes validation for the correct user.
     */
    @Test
    void shouldGenerateValidToken() {
        UserDetails userDetails = userDetailsService.loadUserByUsername(AUSTIN_EMAIL);

        String token = jwtService.generateToken(AUSTIN_EMAIL);
        assertTrue(jwtService.validateToken(token, userDetails));
    }


    // -------------------------------------------------------------------------
    // Expiration
    // -------------------------------------------------------------------------
    /**
     * Verifies that the expiration claim is set to approximately 30 minutes from the time of
     * issuance, within a 5-second tolerance to account for test execution time.
     */
    @Test
    void extractExpiration_ShouldBeApproximatelyThirtyMinutesFromNow() {
        String token = jwtService.generateToken(DYLAN_EMAIL);
        Date expiration = jwtService.extractExpiration(token);
        long diff = expiration.getTime() - System.currentTimeMillis();
        long thirtyMinutesMs = 1000L * 60 * 30;
        // Allow 5 second tolerance for test execution time
        assertTrue(diff > thirtyMinutesMs - 5000 && diff <= thirtyMinutesMs,
                "Expiration should be ~30 minutes from now, but diff was: " + diff + "ms");
    }


    // -------------------------------------------------------------------------
    // Email extraction
    // -------------------------------------------------------------------------
    /**
     * Verifies that the email stored as the subject claim can be correctly extracted from a token.
     */
    @Test
    void extractEmail_ShouldReturnCorrectSubject() {
        String token = jwtService.generateToken(KEATON_EMAIL);
        assertEquals(KEATON_EMAIL, jwtService.extractEmail(token));
    }


    /**
     * Verifies that a valid email is extracted when the token is provided in a properly
     * formatted {@code Authorization: Bearer <token>} header string.
     */
    @Test
    void extractEmailFromHeader_WithValidBearerToken_ShouldReturnEmail() {
        String token = jwtService.generateToken(AUSTIN_EMAIL);
        String header = "Bearer " + token;
        assertEquals(AUSTIN_EMAIL, jwtService.extractEmailFromHeader(header));
    }


    /**
     * Verifies that {@code null} is returned when the header string does not begin with
     * the {@code "Bearer "} prefix, such as when a raw token is passed directly.
     */
    @Test
    void extractEmailFromHeader_WithNoBearerPrefix_ShouldReturnNull() {
        String token = jwtService.generateToken(DYLAN_EMAIL);
        assertNull(jwtService.extractEmailFromHeader(token));
    }


    /**
     * Verifies that {@code null} is returned gracefully when the header contains a
     * {@code "Bearer "} prefix but the token itself is malformed and unparseable.
     */
    @Test
    void extractEmailFromHeader_WithMalformedToken_ShouldReturnNull() {
        assertNull(jwtService.extractEmailFromHeader("Bearer this.is.garbage"));
    }


    // -------------------------------------------------------------------------
    // validation
    // -------------------------------------------------------------------------
    /**
     * Verifies that a token generated for one user does not validate successfully
     * when presented on behalf of a different user.
     */
    @Test
    void validateToken_WithWrongUser_ShouldReturnFalse() {
        String token = jwtService.generateToken(KEATON_EMAIL);
        UserDetails wrongUser = userDetailsService.loadUserByUsername(DYLAN_EMAIL);
        assertFalse(jwtService.validateToken(token, wrongUser));
    }


    /**
     * Verifies that a token with a modified signature is rejected. JJWT may either return
     * {@code false} or throw a {@link io.jsonwebtoken.security.SecurityException} or
     * {@link io.jsonwebtoken.MalformedJwtException} — both outcomes are acceptable.
     */
    @Test
    void validateToken_WithTamperedToken_ShouldThrowOrReturnFalse() {
        String token = jwtService.generateToken(AUSTIN_EMAIL);
        // Replace the entire signature segment with garbage
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".invalidsignatureXXXXXXXXXXXXXXXX";
        UserDetails userDetails = userDetailsService.loadUserByUsername(AUSTIN_EMAIL);
        try {
            boolean result = jwtService.validateToken(tampered, userDetails);
            assertFalse(result, "Tampered token should not validate");
        } catch (Exception e) {
            assertTrue(e instanceof io.jsonwebtoken.security.SecurityException
                    || e instanceof io.jsonwebtoken.MalformedJwtException,
                    "Expected a JWT security exception, got: " + e.getClass().getName());
        }
    }

    // -------------------------------------------------------------------------
    // Key isolation
    // -------------------------------------------------------------------------
    /**
     * Verifies that tokens issued by one {@link JWTService} instance cannot be validated by
     * a separate instance. Because each instance generates a new signing key on construction,
     * this also documents that application restarts will invalidate all previously issued tokens.
     */
    @Test
    void twoInstances_ShouldNotValidateEachOthersTokens() {
        JWTService instanceA = new JWTService();
        JWTService instanceB = new JWTService();

        String tokenFromA = instanceA.generateToken(DYLAN_EMAIL);
        UserDetails userDetails = userDetailsService.loadUserByUsername(DYLAN_EMAIL);

        // instanceB has a different key, so it should reject instanceA's token
        try {
            boolean result = instanceB.validateToken(tokenFromA, userDetails);
            assertFalse(result, "Token from instance A should not validate against instance B");
        } catch (Exception e) {
            assertTrue(e instanceof io.jsonwebtoken.security.SecurityException
                    || e instanceof io.jsonwebtoken.MalformedJwtException,
                    "Expected a JWT security exception, got: " + e.getClass().getName());
        }
    }
}
