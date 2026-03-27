package com.pink.pfa.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.models.User;

class UserControllerTest extends PfaBase {

    private int getUserIdByEmail (String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return user.getUserId();
    }

    private void promoteUserToAdmin(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        userService.promoteToAdmin(user.getUserId());
    }


    // -------------------------------------------------------------------------
    // getUserById
    // -------------------------------------------------------------------------
    @Test
    void getUserById_WithUserToken_ShouldReturn403() {
        int userId = getUserIdByEmail("morgan@pfa.com");

        String token = loginAndGetToken("morgan@pfa.com", "foobar16");
        webTestClient.get().uri("/api/users/" + userId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getUserById_WithAdminToken_ShouldReturn200() {
        int userId = getUserIdByEmail("morgan@pfa.com");
        promoteUserToAdmin("austin@pfa.com");

        String token = loginAndGetToken("austin@pfa.com", "foobar1");
        webTestClient.get().uri("/api/users/" + userId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }


    //@Test
    //void getUserById_WithValidId_ShouldReturn200() {
        
    //}


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
        webTestClient.get().uri("/api/users/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    /**
     * Authorized Admin Access
     */
    @Test
    void authorizedAdminAccess_WithAdminToken_ShouldReturn200() {
        // promote to admin (just in case) and get token
        promoteUserToAdmin("keaton@pfa.com");
        String token = loginAndGetToken("keaton@pfa.com", "foobar13");

        webTestClient.get().uri("/api/users/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
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

        webTestClient.get().uri("/api/users/getAllUsers")
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
}
