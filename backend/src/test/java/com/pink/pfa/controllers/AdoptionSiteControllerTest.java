package com.pink.pfa.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.User;

/**
 * HTTP-layer integration tests for adoption site contributor and admin endpoints.
 *
 * <p>Covers authorization, happy-path responses, error responses, and verifies
 * that status changes are actually persisted after admin actions.
 *
 * <p>Endpoints tested:
 * <ul>
 *   <li>{@code POST /api/contributor/submitSite}</li>
 *   <li>{@code POST /api/admin/approveSite/{id}}</li>
 *   <li>{@code POST /api/admin/denySite/{id}}</li>
 * </ul>
 */
class AdoptionSiteControllerTest extends PfaBase {

    private final WebTestClient webTestClient;
    private AdoptionSite testSite;

    @Autowired
    AdoptionSiteControllerTest(@LocalServerPort int port) {
        this.webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .build();
    }

    @BeforeEach
    void seedSite() {
        testSite = new AdoptionSite();
        testSite.setName("Controller Test Shelter");
        testSite.setUrl("https://controller-test-" + System.nanoTime() + ".org");
        testSite.setEmail("test@controllershelter.org");
        testSite.setPhone("555-7777");
        testSite.setStatus('P');
        testSite = adoptionSiteRepository.save(testSite);
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

    private String getUserToken() {
        return loginAndGetToken("dylan@pfa.com", "foobar12");
    }

    private String getContributorToken() {
        User taylor = userRepository.findByEmail("taylor@pfa.com").orElseThrow();
        userService.promoteToContributor(taylor.getUserId());
        return loginAndGetToken("taylor@pfa.com", "foobar15");
    }

    private String getAdminToken() {
        User keaton = userRepository.findByEmail("keaton@pfa.com").orElseThrow();
        userService.promoteToAdmin(keaton.getUserId());
        return loginAndGetToken("keaton@pfa.com", "foobar13");
    }

    // -------------------------------------------------------------------------
    // POST /api/contributor/submitSite
    // -------------------------------------------------------------------------

    /**
     * Valid token + new URL should return 200 with the created site DTO.
     */
    @Test
    void submitSite_WithValidToken_ShouldReturn200() {
        String token = getContributorToken();
        String uniqueUrl = "https://submit-test-" + System.nanoTime() + ".org";

        webTestClient.post().uri("/api/contributor/submitSite")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "New Shelter",
                    "url": "%s",
                    "email": "info@newshelter.org",
                    "phone": "555-1234"
                }
            """.formatted(uniqueUrl))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.url").isEqualTo(uniqueUrl);
    }

    /**
     * Duplicate URL should return 409 Conflict.
     */
    @Test
    void submitSite_WithDuplicateUrl_ShouldReturn409() {
        String token = getContributorToken();

        webTestClient.post().uri("/api/contributor/submitSite")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "Duplicate Shelter",
                    "url": "%s",
                    "email": "dupe@shelter.org",
                    "phone": "555-0000"
                }
            """.formatted(testSite.getUrl()))
            .exchange()
            .expectStatus().isEqualTo(409);
    }

    /**
     * No token should return 401.
     */
    @Test
    void submitSite_WithNoToken_ShouldReturn401() {
        webTestClient.post().uri("/api/contributor/submitSite")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "Anon Shelter",
                    "url": "https://anon.org",
                    "email": "anon@shelter.org",
                    "phone": "555-0000"
                }
            """)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    // -------------------------------------------------------------------------
    // POST /api/admin/approveSite/{id}
    // -------------------------------------------------------------------------

    /**
     * Admin token + valid ID should return 204 and persist status 'A'.
     */
    @Test
    void approveSite_WithAdminToken_ShouldReturn204AndPersistStatus() {
        String token = getAdminToken();

        webTestClient.patch().uri("/api/admin/approveSite/" + testSite.getSiteId())
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isNoContent();

        AdoptionSite updated = adoptionSiteRepository.findById(testSite.getSiteId()).orElseThrow();
        assertEquals('A', updated.getStatus());
    }

    /**
     * Non-admin token should return 403.
     */
    @Test
    void approveSite_WithUserToken_ShouldReturn403() {
        String token = getUserToken();

        webTestClient.patch().uri("/api/admin/approveSite/" + testSite.getSiteId())
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isForbidden();
    }

    /**
     * Admin token + nonexistent ID should return 404.
     */
    @Test
    void approveSite_WithInvalidId_ShouldReturn404() {
        String token = getAdminToken();

        webTestClient.patch().uri("/api/admin/approveSite/999999")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // POST /api/admin/denySite/{id}
    // -------------------------------------------------------------------------

    /**
     * Admin token + valid ID should return 204 and persist status 'D'.
     */
    @Test
    void denySite_WithAdminToken_ShouldReturn204AndPersistStatus() {
        String token = getAdminToken();

        webTestClient.patch().uri("/api/admin/denySite/" + testSite.getSiteId())
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isNoContent();

        AdoptionSite updated = adoptionSiteRepository.findById(testSite.getSiteId()).orElseThrow();
        assertEquals('D', updated.getStatus());
    }

    /**
     * Non-admin token should return 403.
     */
    @Test
    void denySite_WithUserToken_ShouldReturn403() {
        String token = getUserToken();

        webTestClient.patch().uri("/api/admin/denySite/" + testSite.getSiteId())
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isForbidden();
    }

    /**
     * Admin token + nonexistent ID should return 404.
     */
    @Test
    void denySite_WithInvalidId_ShouldReturn404() {
        String token = getAdminToken();

        webTestClient.patch().uri("/api/admin/denySite/999999")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isNotFound();
    }
}
