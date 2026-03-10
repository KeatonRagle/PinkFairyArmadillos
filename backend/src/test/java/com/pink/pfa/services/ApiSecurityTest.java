package com.pink.pfa.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.config.TestcontainersConfiguration;
import com.pink.pfa.controllers.requests.UserRequest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


/**
 * Authorization and API Security Tests.
 * <p>
 * Covers:
 * <ul>
 *   <li>Unauthorized admin endpoint access by a regular user (403)</li>
 *   <li>Rejected requests using an expired JWT token</li>
 *   <li>Mass user data retrieval blocked for non-admin users (403)</li>
 * </ul>
 */
@Import({TestcontainersConfiguration.class, TestDataConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiSecurityTest {

    private final WebTestClient webTestClient;
    private final UserService userService;
    private final JWTService jwtService;

    @Autowired
    public ApiSecurityTest (@LocalServerPort int port, UserService userService, JWTService jwtService) {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();        
        this.userService = userService;
        this.jwtService = jwtService;
    }


    // -------------------------------------------------------------------------
    // Authorization (Role-Based Access) Tests
    // -------------------------------------------------------------------------

    /**
     * Unauthorized Admin Access
     * <p>
     * Logs in as a standard user and attempts to access the {@code /api/admin} endpoint.
     * Expects HTTP 403 Forbidden — no admin data returned.
     */
    @Test
    void unauthorizedAdminAccess_WithUserToken_ShouldReturn403() {
        String token = userService.verify(new UserRequest("Austin", "austin@pfa.com", "foobar1"));
        webTestClient.get().uri("/api/admin/getAll")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }


    // -------------------------------------------------------------------------
    // API Security Tests
    // -------------------------------------------------------------------------

    /**
     * Token Expiration Test
     * <p>
     * Crafts a JWT that is already expired (using the running service's signing key via
     * reflection) and sends it to a protected endpoint.
     * Expects the request to be rejected with a 4xx status — no authenticated access granted.
     */
    @Test
    void expiredToken_ShouldBeRejectedByProtectedEndpoint() throws Exception {
        Field keyField = JWTService.class.getDeclaredField("key");
        keyField.setAccessible(true);
        String base64Key = (String) keyField.get(jwtService);
        byte[] keyBytes = Decoders.BASE64.decode(base64Key);
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        String expiredToken = Jwts.builder()
                .subject("austin@pfa.com")
                .issuedAt(new Date(System.currentTimeMillis() - 60_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(signingKey)
                .compact();

        webTestClient.get().uri("/api/users/findMe")
                .header("Authorization", "Bearer " + expiredToken)
                .exchange()
                .expectStatus().value(status ->
                    assertTrue(status == 401 || status == 403,
                            "Expired token must be rejected with 401 or 403, got: " + status));
    }

    /**
     * Mass Data Access Attempt
     * <p>
     * Attempts to retrieve all users via the {@code /api/admin/getAll} endpoint
     * while authenticated as a regular (non-admin) user.
     * Expects HTTP 403 Forbidden — endpoint is restricted to admin role only.
     */
    @Test
    void massUserDataAccess_AsRegularUser_ShouldReturn403() {
        String token = userService.verify(new UserRequest("Dylan", "dylan@pfa.com", "foobar12"));
        webTestClient.get().uri("/api/admin/getAll")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }
}
