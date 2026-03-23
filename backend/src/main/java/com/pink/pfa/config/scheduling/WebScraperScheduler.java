package com.pink.pfa.config.scheduling;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.services.AdoptionSiteService;
import com.pink.pfa.services.DatabaseBackupService;
import com.pink.pfa.services.PetService;
import com.pink.pfa.services.WebScraperService;

import jakarta.persistence.NoResultException;

@Component
public class WebScraperScheduler {
    
    private final WebScraperService webScraperService;
    private final PetService petService;
    private final DatabaseBackupService databaseBackupService;
    private final AdoptionSiteService adoptionSiteService;
    
    public WebScraperScheduler(
        WebScraperService webScraperService,
        PetService petService,
        DatabaseBackupService databaseBackupService,
        AdoptionSiteService adoptionSiteService
    ) {
        this.webScraperService = webScraperService;
        this.petService = petService;
        this.databaseBackupService = databaseBackupService;
        this.adoptionSiteService = adoptionSiteService;
    }


    private static final Logger log = LoggerFactory.getLogger(WebScraperScheduler.class);


    @Async
    @Scheduled(cron = "0 0 2 * * *")
    public void updatePets() {
        List<AdoptionSite> sites = adoptionSiteService.findAll();

        databaseBackupService.backup("pre_scrape");

        try {
            // run the webScraperService and extract the list of pet objects
            List<Pet> pets = webScraperService.runScraper(sites);
            petService.sync(pets);

        // gets thrown in webScraperService.runScraper()
        } catch (NoResultException e) {
            log.error("Scheduler Failed to Get Data From WebScraperService");

        // gets thrown in petService.sync()
        } catch (IllegalArgumentException e) {
            log.error("Scheduler Failed to Sync Data Gathered From Scrape to the DataBase");
        }

        databaseBackupService.backup("post_scrape");
    }
    
}
