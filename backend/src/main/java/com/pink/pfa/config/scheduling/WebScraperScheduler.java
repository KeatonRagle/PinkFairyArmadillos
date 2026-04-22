package com.pink.pfa.config.scheduling;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pink.pfa.exceptions.NoAdoptionSitesException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.services.AdoptionSiteService;
import com.pink.pfa.services.DatabaseBackupService;
import com.pink.pfa.services.FeaturedPetService;
import com.pink.pfa.services.PetService;
import com.pink.pfa.services.WebScraperService;

import jakarta.persistence.NoResultException;


/**
 * Scheduled component responsible for automating the nightly pet data pipeline.
 * <p>
 * Every day at 2:00 AM, this scheduler orchestrates the following sequence:
 * <ol>
 *   <li>Takes a pre-scrape database backup as a safety snapshot.</li>
 *   <li>Fetches all approved adoption sites.</li>
 *   <li>Runs the web scraper against each site to collect current pet listings.</li>
 *   <li>Syncs the scraped pets to the database via {@link PetService}.</li>
 *   <li>Takes a post-scrape database backup to capture the updated state.</li>
 * </ol>
 * <p>
 * Each run executes asynchronously to avoid blocking the main application thread.
 * Errors during scraping or syncing are logged and do not interrupt the backup steps.
 */
@Component
public class WebScraperScheduler {
    
    private final WebScraperService webScraperService;
    private final PetService petService;
    private final DatabaseBackupService databaseBackupService;
    private final AdoptionSiteService adoptionSiteService;
    private final FeaturedPetService featuredPetService;
    
    public WebScraperScheduler(
        WebScraperService webScraperService,
        PetService petService,
        DatabaseBackupService databaseBackupService,
        AdoptionSiteService adoptionSiteService,
        FeaturedPetService featuredPetService
    ) {
        this.webScraperService = webScraperService;
        this.petService = petService;
        this.databaseBackupService = databaseBackupService;
        this.adoptionSiteService = adoptionSiteService;
        this.featuredPetService = featuredPetService;
    }


    private static final Logger log = LoggerFactory.getLogger(WebScraperScheduler.class);


    /**
     * Runs the nightly pet data pipeline at 2:00 AM every day.
     * <p>
     * Scrapes all approved adoption sites and syncs the results to the database,
     * bracketed by pre- and post-scrape backups. Failures during scraping or syncing
     * are caught and logged without halting the post-scrape backup.
     */
    @Async
    @Scheduled(cron = "0 0 2 * * *")
    public void updatePets() {
        databaseBackupService.backup("pre_scrape");

        try {
            // run the webScraperService and extract the list of pet objects
            List<AdoptionSite> sites = adoptionSiteService.findAllForScrape();
            List<Pet> pets = webScraperService.runScraper(sites);
            petService.sync(pets);

        // gets thrown in webScraperService.runScraper()
        } catch (NoAdoptionSitesException e) {
            log.error("There are no AdoptionSites approved for scrape in the database");
        } catch (NoResultException e) {
            log.error("Scheduler Failed to Get Data From WebScraperService");
        // gets thrown in petService.sync()
        } catch (IllegalArgumentException e) {
            log.error("Scheduler Failed to Sync Data Gathered From Scrape to the DataBase");
        }

        databaseBackupService.backup("post_scrape");

        // Set up featured pets...
        featuredPetService.setupFeaturedByCount(1, 1);
    }
}
