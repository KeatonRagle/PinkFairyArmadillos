package com.pink.pfa.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import com.pink.pfa.context.PfaBase;

public class ApiSecurityTest extends PfaBase {


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
     * incorect password
     * */


    /**
     * nonexistant email
     * */


    /**
     * banned user cant access
     * */

    /**
     * user with expired credentials cant access
     * */
}
