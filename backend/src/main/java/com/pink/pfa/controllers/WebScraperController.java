package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pink.pfa.exceptions.NoAdoptionSitesException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.services.AdoptionSiteService;
import com.pink.pfa.services.DatabaseBackupService;
import com.pink.pfa.services.PetService;
import com.pink.pfa.services.WebScraperService;

@EnableMethodSecurity
@RestController
@RequestMapping("/api/webScraper")
public class WebScraperController {

    private static final Logger log = LoggerFactory.getLogger(PetService.class);

    private final PetService petService;
    private final WebScraperService webScraperService;
    private final DatabaseBackupService databaseBackupService;
    private final AdoptionSiteService adoptionSiteService;

    public WebScraperController (
        PetService petService,
        WebScraperService webScraperService, 
        DatabaseBackupService databaseBackupService,
        AdoptionSiteService adoptionSiteService
    ) {
        this.petService = petService;
        this.webScraperService = webScraperService;
        this.databaseBackupService = databaseBackupService;
        this.adoptionSiteService = adoptionSiteService;
    }

    /**
     * Returns a single pet by its ID, along with a UTC timestamp.
     *
     * @param id the unique identifier of the pet
     * @return a map containing the matched {@code Pet} and a {@code Timestamp} string
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/scrape")
    public ResponseEntity<String> scrapeForPets() {
        try {
            List<AdoptionSite> sites = adoptionSiteService.findAllForScrape();
            databaseBackupService.backup("pre_scrape");
            List<Pet> scrapedPets = webScraperService.runScraper(sites);
            petService.sync(scrapedPets);
            databaseBackupService.backup("post_scrape");
            return ResponseEntity.ok("Scrape complete. Synced " + scrapedPets.size() + " pets.");
        } catch (NoAdoptionSitesException e) {
            log.error("Scrape failed", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        } catch (Exception e) {
            log.error("Scrape failed", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    
}
