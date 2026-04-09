package com.pink.pfa.controllers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

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

    private AdoptionSite testSite;

    @BeforeEach
    void seedSite() {
        testSite = new AdoptionSite();
        testSite.setName("Controller Test Shelter");
        testSite.setUrl("https://controller-test-" + System.nanoTime() + ".org");
        testSite.setEmail("test@controllershelter.org");
        testSite.setPhone("555-7777");
        testSite.setStatus('P');
        testSite.setSubmittedAt(LocalDate.now());
        testSite.setUser(getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR).user());
        testSite = adoptionSiteRepository.save(testSite);
    }

    // -------------------------------------------------------------------------
    // POST /api/contributor/submitSite
    // -------------------------------------------------------------------------

    /**
     * Valid token + new URL should return 200 with the created site DTO.
     */
    @Test
    void submitSite_WithValidToken_ShouldReturn200() {
        SeededUser contributor = getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR);
        System.out.println(contributor.user().getName());
        String token = loginAndGetToken(contributor.user().getEmail(), contributor.password());
        String uniqueUrl = "https://submit-test-" + System.nanoTime() + ".org";
        String bVal = """
            {
                "name": "New Shelter",
                "url": "%s",
                "email": "info@newshelter.org",
                "phone": "555-1234",
                "rating": 0.8,
                "userID": %d
            }
        """.formatted(uniqueUrl, contributor.user().getUserId());
        
        webTestClient.post().uri("/api/adoptionSite/submitSite")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(bVal)
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR);
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.post().uri("/api/adoptionSite/submitSite")
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
        webTestClient.post().uri("/api/adoptionSite/submitSite")
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.patch().uri("/api/adoptionSite/approveSite/" + testSite.getSiteId())
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.patch().uri("/api/adoptionSite/approveSite/" + testSite.getSiteId())
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isForbidden();
    }

    /**
     * Admin token + nonexistent ID should return 404.
     */
    @Test
    void approveSite_WithInvalidId_ShouldReturn404() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.patch().uri("/api/adoptionSite/approveSite/999999")
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.patch().uri("/api/adoptionSite/denySite/" + testSite.getSiteId())
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.patch().uri("/api/adoptionSite/denySite/" + testSite.getSiteId())
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isForbidden();
    }

    /**
     * Admin token + nonexistent ID should return 404.
     */
    @Test
    void denySite_WithInvalidId_ShouldReturn404() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.patch().uri("/api/adoptionSite/denySite/999999")
            .header("Authorization", "Bearer " + token)
            .exchange()
            .expectStatus().isNotFound();
    }
}
