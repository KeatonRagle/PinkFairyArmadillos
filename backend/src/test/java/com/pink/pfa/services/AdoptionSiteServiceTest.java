package com.pink.pfa.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.controllers.requests.AdoptionSiteRequest;
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
    // findAllForScrape
    // -------------------------------------------------------------------------

    /**
     * Verifies that only sites with status 'A' are returned.
     */
    @Test
    void findAllForScrape_ShouldOnlyReturnApprovedSites() {
        // Approve our test site
        testSite.setStatus('A');
        adoptionSiteRepository.save(testSite);

        List<AdoptionSite> approved = adoptionSiteService.findAllForScrape();

        assertTrue(approved.stream().allMatch(s -> s.getStatus() == 'A'),
            "All returned sites should have status 'A'");
    }

    /**
     * Verifies that pending and denied sites are excluded from findAllApproved results.
     */
    @Test
    void findAllForScrape_ShouldExcludePendingAndDeniedSites() {
        // testSite is 'P' by default — should not appear
        List<AdoptionSite> approved = adoptionSiteService.findAllForScrape();

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
    // findApproved
    // -------------------------------------------------------------------------

    /**
     * Verifies that only sites with status 'A' are returned.
     */
    @Test
    void findApproved_ShouldOnlyReturnApprovedSites() {
        List<AdoptionSiteDTO> sites = adoptionSiteService.findApproved();

        assertTrue(sites.stream().allMatch(s -> s.status() == 'A'),
            "All returned sites should have status 'A'");
    }


    /**
     * Verifies that pending and denied sites are excluded from findApproved results.
     */
    @Test
    void findApproved_ShouldExcludePendingAndDeniedSites() {
        List<AdoptionSiteDTO> sites = adoptionSiteService.findApproved();

        assertFalse(sites.stream().anyMatch(s -> s.status() == 'D' || s.status() == 'P'),
            "No returned sites should have a status of 'D' or 'P'");
    }

    // -------------------------------------------------------------------------
    // findDenied
    // -------------------------------------------------------------------------

    /**
     * Verifies that only sites with status 'D' are returned.
     */
    @Test
    void findDenied_ShouldOnlyReturnDeniedSites() {
        List<AdoptionSiteDTO> sites = adoptionSiteService.findDenied();

        assertTrue(sites.stream().allMatch(s -> s.status() == 'D'),
            "All returned sites should have status 'D'");
    }


    /**
     * Verifies that pending and denied sites are excluded from findDenied results.
     */
    @Test
    void findDenied_ShouldExcludePendingAndApprovedSites() {
        List<AdoptionSiteDTO> sites = adoptionSiteService.findDenied();

        assertFalse(sites.stream().anyMatch(s -> s.status() == 'A' || s.status() == 'P'),
            "No returned sites should have a status of 'A' or 'P'");
    }

    // -------------------------------------------------------------------------
    // findPending
    // -------------------------------------------------------------------------

    /**
     * Verifies that only sites with status 'P' are returned.
     */
    @Test
    void findPending_ShouldOnlyReturnPendingSites() {
        List<AdoptionSiteDTO> sites = adoptionSiteService.findPending();

        assertTrue(sites.stream().allMatch(s -> s.status() == 'P'),
            "All returned sites should have status 'D'");
    }


    /**
     * Verifies that approved and denied sites are excluded from findPending results.
     */
    @Test
    void findPending_ShouldExcludeDeniedAndApprovedSites() {
        List<AdoptionSiteDTO> sites = adoptionSiteService.findPending();

        assertFalse(sites.stream().anyMatch(s -> s.status() == 'A' || s.status() == 'D'),
            "No returned sites should have a status of 'A' or 'D'");
    }

    // -------------------------------------------------------------------------
    // submitNewSite
    // -------------------------------------------------------------------------

    /**
     * Verifies that a new site is saved and returned as a DTO when the URL is unique.
     */
    @Test
    void submitNewSite_ShouldSaveAndReturnDTO_WhenUrlIsNew() {
        AdoptionSiteRequest request = new AdoptionSiteRequest(
            "https://brand-new-shelter-" + System.nanoTime() + ".org",
            "New Shelter",
            "info@newshelter.org",
            "555-0001",
            0.0
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
        AdoptionSiteRequest duplicate = new AdoptionSiteRequest(
            testSite.getUrl(),
            "Duplicate Shelter",
            "other@email.org",
            "555-0002",
            0.0
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
    // approveSite
    // -------------------------------------------------------------------------
 
    /**
     * Verifies that edits made to a site are persisted to the database
     * */
    @Test
    void editSite_ShouldPersistChanges() {
        AdoptionSite old = adoptionSiteRepository.findById(testSite.getSiteId()).orElseThrow();

        AdoptionSiteRequest request = new AdoptionSiteRequest(
            "https://edited-shelter-" + System.nanoTime() + ".org",
            "Edited Shelter",
            "info@editedshelter.org",
            "555-0003",
            0.2
        );
        adoptionSiteService.editSite(request, testSite.getSiteId());
        AdoptionSite updated = adoptionSiteRepository.findById(testSite.getSiteId()).orElseThrow();

        assertEquals(old.getSiteId(), updated.getSiteId());
        assertNotEquals(old.getUrl(), updated.getUrl());
        assertNotEquals(old.getName(), updated.getName());
        assertNotEquals(old.getEmail(), updated.getEmail());
        assertNotEquals(old.getPhone(), updated.getPhone());
    }


    /**
     * Verifies that approving a nonexistent ID throws ResourceNotFoundException.
     */
    @Test
    void editSite_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {
        AdoptionSiteRequest request = new AdoptionSiteRequest(
            "https://edited-shelter-" + System.nanoTime() + ".org",
            "Edited Shelter",
            "info@editedshelter.org",
            "555-0004",
            0.3
        );
        assertThrows(ResourceNotFoundException.class,
            () -> adoptionSiteService.editSite(request, 999999));
    }


    // -------------------------------------------------------------------------
    // approveSite
    // -------------------------------------------------------------------------

    /**
     * Verifies that approving a site persists status 'A' to the database.
     */
    @Test
    void approveSite_ShouldPersistApprovedStatus() {
        adoptionSiteService.approveSite(testSite.getSiteId());

        AdoptionSite updated = adoptionSiteRepository.findById(testSite.getSiteId()).orElseThrow();
        assertEquals('A', updated.getStatus());
    }

    /**
     * Verifies that approving a nonexistent ID throws ResourceNotFoundException.
     */
    @Test
    void approveSite_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {
        assertThrows(ResourceNotFoundException.class,
            () -> adoptionSiteService.approveSite(999999));
    }

    // -------------------------------------------------------------------------
    // denySite
    // -------------------------------------------------------------------------

    /**
     * Verifies that denying a site persists status 'D' to the database.
     */
    @Test
    void denySite_ShouldPersistDeniedStatus() {
        adoptionSiteService.denySite(testSite.getSiteId());

        AdoptionSite updated = adoptionSiteRepository.findById(testSite.getSiteId()).orElseThrow();
        assertEquals('D', updated.getStatus());
    }

    /**
     * Verifies that denying a nonexistent ID throws ResourceNotFoundException.
     */
    @Test
    void denySite_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {
        assertThrows(ResourceNotFoundException.class,
            () -> adoptionSiteService.denySite(999999));
    }
}
