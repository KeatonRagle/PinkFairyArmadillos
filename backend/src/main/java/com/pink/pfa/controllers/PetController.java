package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.datatransfer.PetDTO;
import com.pink.pfa.services.AdoptionSiteService;
import com.pink.pfa.services.DatabaseBackupService;
import com.pink.pfa.services.PetService;
import com.pink.pfa.services.WebScraperService;


/**
 * REST controller exposing the {@code /api/pets} API surface for the Pets for All platform.
 *
 * <p>This controller serves as the primary entry point for pet data interactions,
 * handling both database-backed retrieval and on-demand web scraping from external
 * adoption sites. It is intended to be consumed by the React/Vite frontend or any
 * authorized API client.
 *
 * <p>Use this controller when you need to:
 * <ul>
 *   <li>Fetch all available pet listings stored in the database</li>
 *   <li>Look up a specific pet record by its unique ID</li>
 *   <li>Trigger a live scrape of external adoption sites to populate or refresh pet data</li>
 * </ul>
 *
 * <p>All business logic is delegated to {@link PetService}; this controller is
 * responsible only for request mapping, response shaping, and timestamp injection.
 */
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;
    private final WebScraperService webScraperService;
    private final DatabaseBackupService databaseBackupService;
    private final AdoptionSiteService adoptionSiteService;

    public PetController (
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
     * Returns all pets currently stored in the database, along with a UTC timestamp.
     *
     * @return a map containing a {@code Pets} list and a {@code Timestamp} string
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<PetDTO>> getAllPets() {
        try {
            return ResponseEntity.ok().body(petService.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

 
    /**
     * Returns a single pet by its ID, along with a UTC timestamp.
     *
     * @param id the unique identifier of the pet
     * @return a map containing the matched {@code Pet} and a {@code Timestamp} string
     */
    @GetMapping("/{id}")
    public ResponseEntity<PetDTO> getPetById(
        @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok().body(petService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();           
        }
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
            List<AdoptionSite> sites = adoptionSiteService.findAll();
            databaseBackupService.backup("pre_scrape");
            List<Pet> scrapedPets = webScraperService.runScraper(sites);
            petService.sync(scrapedPets);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
