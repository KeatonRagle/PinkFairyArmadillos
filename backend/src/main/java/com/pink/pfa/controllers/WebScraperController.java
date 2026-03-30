package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.services.AdoptionSiteService;
import com.pink.pfa.services.DatabaseBackupService;
import com.pink.pfa.services.PetService;
import com.pink.pfa.services.WebScraperService;

@EnableAsync
@EnableMethodSecurity
@RestController
@RequestMapping("/api/webScraper")
public class WebScraperController {

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
    @GetMapping("/scrape")
    public ResponseEntity<Void> scrapeForPets () {
        try {
            List<AdoptionSite> sites = adoptionSiteService.findAllForScrape();
            databaseBackupService.backup("pre_scrape");
            List<Pet> scrapedPets = webScraperService.runScraper(sites);
            petService.sync(scrapedPets);
            databaseBackupService.backup("post_scrape");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
}
