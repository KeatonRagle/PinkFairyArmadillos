package com.pink.pfa.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.controllers.requests.NewAdoptionSiteRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.SiteAlreadyExistsException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.datatransfer.AdoptionSiteDTO;

/**
 * Integration test suite for {@link AdoptionSiteService}.
 *
 * <p>Runs against a real Testcontainers MySQL instance via {@link PfaBase}.
 * Each test seeds its own site data and verifies that status transitions,
 * duplicate detection, and DTO mapping behave correctly against a real database.
 */
class AdoptionSiteServiceTest extends PfaBase {

    private AdoptionSite testSite;

    @BeforeEach
    void seedSite() {
        testSite = new AdoptionSite();
        testSite.setName("Test Shelter");
        testSite.setUrl("https://unique-test-shelter-" + System.nanoTime() + ".org");
        testSite.setEmail("contact@testshelter.org");
        testSite.setPhone("555-9999");
        testSite.setStatus('P');
        testSite = adoptionSiteRepository.saveAndFlush(testSite);
    }

    // -------------------------------------------------------------------------
    // findAllApproved
    // -------------------------------------------------------------------------

    /**
     * Verifies that only sites with status 'A' are returned.
     */
    @Test
    void findAllApproved_ShouldOnlyReturnApprovedSites() {
        // Approve our test site
        testSite.setStatus('A');
        adoptionSiteRepository.save(testSite);

        List<AdoptionSite> approved = adoptionSiteService.findAllApproved();

        assertTrue(approved.stream().allMatch(s -> s.getStatus() == 'A'),
            "All returned sites should have status 'A'");
    }

    /**
     * Verifies that pending sites are excluded from findAllApproved results.
     */
    @Test
    void findAllApproved_ShouldExcludePendingAndDeniedSites() {
        // testSite is 'P' by default — should not appear
        List<AdoptionSite> approved = adoptionSiteService.findAllApproved();

        boolean containsTestSite = approved.stream()
            .anyMatch(s -> s.getSiteId().equals(testSite.getSiteId()));

        assertTrue(!containsTestSite, "Pending site should not appear in approved results");
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    /**
     * Verifies that findAll returns all sites mapped to DTOs with populated fields.
     */
    @Test
    void findAll_ShouldReturnAllSitesAsDTOs() {
        List<AdoptionSiteDTO> sites = adoptionSiteService.findAll();

        assertTrue(sites.size() >= 1, "Expected at least the seeded test site");
        sites.forEach(s -> assertNotNull(s.url(), "URL should not be null"));
    }

    // -------------------------------------------------------------------------
    // submitNewSite
    // -------------------------------------------------------------------------

    /**
     * Verifies that a new site is saved and returned as a DTO when the URL is unique.
     */
    @Test
    void submitNewSite_ShouldSaveAndReturnDTO_WhenUrlIsNew() {
        NewAdoptionSiteRequest request = new NewAdoptionSiteRequest(
            "https://brand-new-shelter-" + System.nanoTime() + ".org",
            "New Shelter",
            "info@newshelter.org",
            "555-0001"
        );

        AdoptionSiteDTO result = adoptionSiteService.submitNewSite(request);

        assertNotNull(result);
        assertEquals(request.url(), result.url());
        assertEquals(request.name(), result.name());
    }

    /**
     * Verifies that submitting a duplicate URL throws SiteAlreadyExistsException
     * and does not create a second record.
     */
    @Test
    void submitNewSite_ShouldThrowSiteAlreadyExistsException_WhenUrlIsDuplicate() {
        NewAdoptionSiteRequest duplicate = new NewAdoptionSiteRequest(
            testSite.getUrl(),
            "Duplicate Shelter",
            "other@email.org",
            "555-0002"
        );

        assertThrows(SiteAlreadyExistsException.class,
            () -> adoptionSiteService.submitNewSite(duplicate));

        long count = adoptionSiteRepository.findAll()
            .stream()
            .filter(s -> s.getUrl().equals(testSite.getUrl()))
            .count();

        assertEquals(1, count, "Should be exactly one site with this URL");
    }

    // -------------------------------------------------------------------------
    // approveNewSiteRequest
    // -------------------------------------------------------------------------

    /**
     * Verifies that approving a site persists status 'A' to the database.
     */
    @Test
    void approveNewSiteRequest_ShouldPersistApprovedStatus() {
        adoptionSiteService.approveNewSiteRequest(testSite.getSiteId());

        AdoptionSite updated = adoptionSiteRepository.findById(testSite.getSiteId()).orElseThrow();
        assertEquals('A', updated.getStatus());
    }

    /**
     * Verifies that approving a nonexistent ID throws ResourceNotFoundException.
     */
    @Test
    void approveNewSiteRequest_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {
        assertThrows(ResourceNotFoundException.class,
            () -> adoptionSiteService.approveNewSiteRequest(999999));
    }

    // -------------------------------------------------------------------------
    // denyNewSiteRequest
    // -------------------------------------------------------------------------

    /**
     * Verifies that denying a site persists status 'D' to the database.
     */
    @Test
    void denyNewSiteRequest_ShouldPersistDeniedStatus() {
        adoptionSiteService.denyNewSiteRequest(testSite.getSiteId());

        AdoptionSite updated = adoptionSiteRepository.findById(testSite.getSiteId()).orElseThrow();
        assertEquals('D', updated.getStatus());
    }

    /**
     * Verifies that denying a nonexistent ID throws ResourceNotFoundException.
     */
    @Test
    void denyNewSiteRequest_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {
        assertThrows(ResourceNotFoundException.class,
            () -> adoptionSiteService.denyNewSiteRequest(999999));
    }
}
