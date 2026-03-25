package com.pink.pfa.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.datatransfer.PetDTO;
import com.pink.pfa.repos.AdoptionSiteRepository;
import com.pink.pfa.repos.PetRepository;

import jakarta.transaction.Transactional;


/**
 * PetService<br>
 * <br>
 * Central service layer for pet-related business logic.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Read pets from the database via {@link PetRepository}.</li>
 *   <li>Convert {@link Pet} entities to {@link PetDTO} objects to format for frontend
 *       (e.g., passwords) to controllers / clients.</li>
 * </ul>
 *
 * Notes:
 * <ul>
 *   <li>This class is annotated with {@link Service}, meaning it is a Spring-managed singleton component.</li>
 *   <li>DTO mapping is used so API responses can safely expose only the fields the client should see.</li>
 * </ul>
 */
@Service
@RequestMapping("/api/pets")
public class PetService {

    private static final Logger log = LoggerFactory.getLogger(PetService.class);
    private final PetRepository petRepository;
    private final AdoptionSiteRepository adoptionRepository;

    public PetService (PetRepository petRepository, AdoptionSiteRepository adoptionRepository) {
        this.petRepository = petRepository;
        this.adoptionRepository = adoptionRepository;
    }

    /**
     * Returns all pets as a list of {@link PetDTO}s by fetching entities from the database and mapping
     * each {@link Pet} to a DTO to avoid exposing sensitive fields.
     *
     * @return list of {@link PetDTO}
     */
    public List<PetDTO> findAll() {
        return petRepository.findAll()
            .stream()
            .map(PetDTO::fromEntity)
            .toList();
    }


    /**
     * Fetches a single pet by ID and returns it as a {@link PetDTO}.
     * Throws an exception if the pet does not exist.
     *
     * @param id database ID of the pet
     * @return {@link PetDTO} for the requested pet
     */    
    public PetDTO findById(Integer id) {
        return petRepository.findById(id)
            .map(PetDTO::fromEntity)
            .orElseThrow(() -> new InvalidConfigurationPropertyValueException("Failed to Find ID", null, "Pet Not Found"));
    }

    /**
     * Fetches all pets by name and returns it as a list of {@link PetDTO}.
     * Throws an exception if the pet does not exist.
     *
     * @param name Name of the pet
     * @return list of {@link PetDTO} for the requested name
     */    
    public List<PetDTO> findByName(String name) {
        return petRepository.findByName(name)
            .stream()
            .map(PetDTO::fromEntity)
            .toList();
    }

    public void sync(List<Pet> scrapedPets) {        
        // Group scraped pets by site so each site is synced independently
        // Map<Integer, List<Pet>> bySite = scrapedPets.stream()
        //     .collect(Collectors.groupingBy(p -> p.getSite().getSiteId()));

        // for (Map.Entry<Integer, List<Pet>> entry : bySite.entrySet()) {
        //     syncBySite(entry.getKey(), entry.getValue());
        // }

        // Create two maps, one for the scraped pets and one for all in the db
        // keyed by the custom key method as a hash
        // Then, run a diff on the two maps by checking to see if any differences exist
        // between entries in the scrape and existing elements in the DB
        Map<String, Pet> scrapedMap = scrapedPets
            .stream()
            .collect(Collectors.toMap(this::buildKey, p -> p));

        Map<String, Pet> dbMap = petRepository.findAll()
            .stream()
            .collect(Collectors.toMap(this::buildKey, p -> p));

        // In scrape — add or update
        for (Map.Entry<String, Pet> entry : scrapedMap.entrySet()) {
            Pet scraped = entry.getValue();
            Pet existing = dbMap.get(entry.getKey());
            
            if (existing == null) {
                // Naturally, if not in, add it
                petRepository.save(scraped);
                log.info("Added new pet: {}", entry.getKey());
            } else if (hasChanges(existing, scraped)) {
                // Otherwise, if it has sufficient changes in the fields that matter, 
                // apply those changes and update the entry in the DB
                applyUpdates(existing, scraped);
                petRepository.save(existing);
                log.info("Updated pet: {}", entry.getKey());
            }
        }

        // In DB but not in scrape — deactivate
        for (Map.Entry<String, Pet> entry : dbMap.entrySet()) {
            if (!scrapedMap.containsKey(entry.getKey())) {
                Pet missing = entry.getValue();
                missing.setPetStatus("INACTIVE");
                petRepository.save(missing);
                log.info("Deactivated pet: {}", entry.getKey());
            }
        }
    }

    //Experimental...
    @Transactional
    public void syncBySite(Integer siteId, List<Pet> scrapedPets) {
        AdoptionSite site = adoptionRepository.findById(siteId)
            .orElseThrow(() -> new IllegalArgumentException("Site not found: " + siteId));

        Map<String, Pet> scrapedMap = scrapedPets.stream()
            .collect(Collectors.toMap(this::buildKey, p -> p));

        Map<String, Pet> dbMap = petRepository.findBySite_SiteId(siteId)
            .stream()
            .collect(Collectors.toMap(this::buildKey, p -> p));

        // In scrape — add or update
        for (Map.Entry<String, Pet> entry : scrapedMap.entrySet()) {
            Pet scraped = entry.getValue();
            scraped.setSite(site);

            Pet existing = dbMap.get(entry.getKey());
            if (existing == null) {
                petRepository.save(scraped);
                log.info("Added new pet: {}", entry.getKey());
            } else if (hasChanges(existing, scraped)) {
                applyUpdates(existing, scraped);
                petRepository.save(existing); 
                log.info("Updated pet: {}", entry.getKey());
            }
        }

        // In DB but not in scrape — deactivate
        for (Map.Entry<String, Pet> entry : dbMap.entrySet()) {
            if (!scrapedMap.containsKey(entry.getKey())) {
                Pet missing = entry.getValue();
                missing.setPetStatus("INACTIVE");
                petRepository.save(missing);
                log.info("Deactivated pet: {}", entry.getKey());
            }
        }
    }

    // Build a composite key using attributes that, collectively, should never be repeated
    // by the law of statistics (I guess?)
    private String buildKey(Pet pet) {
        return String.join("__",
            pet.getSite().getSiteId().toString().trim(),
            pet.getName().toLowerCase().trim(),
            pet.getBreed().toLowerCase().trim(),
            pet.getPetType().toLowerCase().trim(),
            String.valueOf(pet.getGender()).toLowerCase().trim(),
            Integer.toString(pet.getAge()).trim()
        ).replaceAll("\\s+", " ");
    }

    // If location, price, or status has changes between the new scrape and the old DB,
    // update the DB entry
    private boolean hasChanges(Pet existing, Pet scraped) {
        return !existing.getLocation().equals(scraped.getLocation())
            || existing.getPrice() != scraped.getPrice()
            || !existing.getPetStatus().equals(scraped.getPetStatus());
    }

    // Only update scraper-owned fields
    private void applyUpdates(Pet existing, Pet scraped) {
        existing.setLocation(scraped.getLocation());
        existing.setPrice(scraped.getPrice());
        existing.setPetStatus(scraped.getPetStatus());
    }
}
