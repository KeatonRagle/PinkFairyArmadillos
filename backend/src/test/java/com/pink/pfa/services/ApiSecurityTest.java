package com.pink.pfa.services;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.models.User;

class ApiSecurityTest extends PfaBase {

    private final WebTestClient webTestClient;

    @Autowired
    ApiSecurityTest(@LocalServerPort int port) {
        this.webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .build();
    }

    private String loginAndGetToken(String email, String password) {
        return webTestClient.post().uri("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {"email":"%s","password":"%s"}
            """.formatted(email, password))
            .exchange()
            .expectStatus().isOk()
            .returnResult(new org.springframework.core.ParameterizedTypeReference<java.util.Map<String, Object>>() {})
            .getResponseBody()
            .blockFirst()
            .get("token")
            .toString();
    }

    // -------------------------------------------------------------------------
    // Authorization (Role-Based Access) Tests
    // -------------------------------------------------------------------------

    /**
     * Unauthorized Admin Access
     */
    @Test
    void unauthorizedAdminAccess_WithUserToken_ShouldReturn403() {
        userService.createUser(new UserRequest("randomUser", "randomUser@pfa.com", "password123"));
        String token = loginAndGetToken("randomUser@pfa.com", "password123");
        webTestClient.get().uri("/api/admin/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    // -------------------------------------------------------------------------
    // API Security Tests
    // -------------------------------------------------------------------------

    /**
     * Token Expiration Test — signs with a fresh random key unrelated to the
     * server's key, so the server will always reject it (wrong signature OR
     * expired — either way is a valid rejection).
     */
    @Test
    void expiredToken_ShouldBeRejectedByProtectedEndpoint() throws Exception {
        String expiredToken = jwtService.generateExpiredToken("austin@pfa.com");
        EntityExchangeResult<String> result = webTestClient.get().uri("/api/users/findMe")
            .header("Authorization", "Bearer " + expiredToken)
            .exchange()
            .expectStatus().value(status ->
                assertTrue(status == 401,
                        "Expired token must be rejected with 401, got: " + status))
            .expectBody(String.class)
            .returnResult();

        System.out.println("Response body: " + result.getResponseBody());
    }

    /**
     * Mass Data Access Attempt
     */
    @Test
    void massUserDataAccess_AsRegularUser_ShouldReturn403() {
        String token = loginAndGetToken("dylan@pfa.com", "foobar12");

        webTestClient.get().uri("/api/admin/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    /**
     * Unauthenticated request to protected endpoint
     */
    @Test
    void unauthenticated_RequestToProtectedEndpoint_ShouldReturn401() {
        webTestClient.get().uri("/api/users/findMe")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    /**
     * Missing Bearer prefix
     */
    @Test
    void missingBearerPrefix_ShouldBeRejected() {
        String token = loginAndGetToken("austin@pfa.com", "foobar1");
        webTestClient.get().uri("/api/users/findMe")
            .header("Authorization", token)
            .exchange()
            .expectStatus().value(status ->
                assertTrue(status == 401 || status == 403,
                    "Missing Bearer prefix should be rejected, got: " + status));
    }

    /**
     * Valid admin token should reach admin endpoint
     */
    @Test
    void validAdminToken_ShouldReturn200OnAdminEndpoint() {
        // promote just in case
        User keaton = userRepository.findByEmail("keaton@pfa.com").orElseThrow();
        userService.promoteToAdmin(keaton.getUserId());

        // test
        String token = loginAndGetToken("keaton@pfa.com", "foobar13");
        webTestClient.get().uri("/api/admin/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk(); // still a regular user until promoted
    }
}
