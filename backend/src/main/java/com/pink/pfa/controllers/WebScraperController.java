package com.pink.pfa.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @GetMapping(value = "/scrape", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter scrapeForPets() {
        SseEmitter emitter = new SseEmitter(0L); // 0 = no timeout

        CompletableFuture.runAsync(() -> {
            try {
                emit(emitter, "Starting scrape...");

                List<AdoptionSite> sites = adoptionSiteService.findAllForScrape();
                emit(emitter, "Found " + sites.size() + " adoption sites");

                emit(emitter, "Creating pre-scrape backup...");
                databaseBackupService.backup("pre_scrape");
                emit(emitter, "Pre-scrape backup complete");

                emit(emitter, "Running scraper...");
                List<Pet> scrapedPets = webScraperService.runScraper(sites);
                emit(emitter, "Scraper returned " + scrapedPets.size() + " pets");

                emit(emitter, "Syncing pets to database...");
                petService.sync(scrapedPets);
                emit(emitter, "Sync complete");

                emit(emitter, "Creating post-scrape backup...");
                databaseBackupService.backup("post_scrape");
                emit(emitter, "Post-scrape backup complete");

                emit(emitter, "DONE");
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("ERROR: " + e.getMessage()));
                } catch (IOException ex) {
                    log.error("Failed to send error event", ex);
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private void emit(SseEmitter emitter, String message) {
        try {
            log.info("[SCRAPE] {}", message);
            emitter.send(SseEmitter.event().data("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message));
        } catch (IOException e) {
            log.error("Failed to send SSE event", e);
        }
    }
    
}
