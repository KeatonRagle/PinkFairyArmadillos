package com.pink.pfa.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.controllers.requests.PetRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.PetImage;
import com.pink.pfa.models.UserPreferences;
import com.pink.pfa.models.datatransfer.PetDTO;
import com.pink.pfa.models.datatransfer.UserDTO;
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
    @Autowired private UserService userService;
    @Autowired private UserPrefService userPrefService;

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

    public List<PetDTO> findAllActive() {
        return petRepository.findByPetStatusNot("INACTIVE")
            .stream()
            .map(PetDTO::fromEntity)
            .toList();
    }

    public PetDTO findRandomActivePetByType(String type) {
        return petRepository.findByPetStatusNotAndTypeRand(type, "INACTIVE")
            .map(PetDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("No active pets found", 0));
    }

    public PetDTO addPet(PetRequest request) {
        Pet pet = new Pet(request.name(), request.breed(), request.age(), request.gender(), 
            request.petType(), request.location(), request.price(), request.size(), 
            request.petStatus(), request.compatibilityScore(), request.imgUrl(), LocalDate.now()
        );

        pet.setSite(adoptionRepository.findById(request.siteId())
            .orElseThrow(() -> new ResourceNotFoundException("AdoptionSite", request.siteId()))
        );

        Pet savedPet = petRepository.save(pet);
        return PetDTO.fromEntity(savedPet);
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
            .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
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

    private int scorePet(Pet pet, List<UserPreferences> prefs) {
        int score = 0;

        for (var pref : prefs) {
            String val = pref.getPrefValue();

            boolean matches = switch (pref.getPrefTrait()) {
                case BREED   -> pet.getBreed().toLowerCase().contains(val.toLowerCase());
                case GENDER  -> String.valueOf(pet.getGender()).equalsIgnoreCase(val);
                case SIZE    -> pet.getSize().equalsIgnoreCase(val);
                case PET_TYPE-> pet.getPetType().equalsIgnoreCase(val);
                default -> false;
            };

            if (pref.getPrefTrait() == UserPreferences.Preference.AGE_MIN || pref.getPrefTrait() == UserPreferences.Preference.AGE_MAX) {
                String[] ageComponents = val.split(" ");
                int actualAge = switch (ageComponents[1]) {
                    case "Weeks" -> Integer.parseInt(ageComponents[0]);
                    case "Months" -> Integer.parseInt(ageComponents[0]) * 4;
                    case "Years" -> Integer.parseInt(ageComponents[0]) * 52;
                    default -> Integer.parseInt(ageComponents[0]);
                };

                matches = (pref.getPrefTrait() == UserPreferences.Preference.AGE_MAX && pet.getAge() <= actualAge) ||
                          (pref.getPrefTrait() == UserPreferences.Preference.AGE_MIN && pet.getAge() >= actualAge);
            }

            if (matches) {
                score++;
            }
        }

        return score;
    }

    /**
     * Fetches all pets by a given set of filters and returns it as a list of {@link PetDTO}.
     * Throws an exception if the no pets exist.
     *
     * @param petType Type of the pet
     * @param gender Gender of the pet
     * @param startAge Starting Age range of the pet
     * @param endAge Ending Age range of the pet
     * @param breed Breed of the pet
     * @param size Size of the pet
     * @param filterPrefs Flag that tells us if we're supposed to filter by preferences
     * @return list of {@link PetDTO} for the requested name
     */ 
    public List<PetDTO> findByFilter(String petType, String gender, Integer startAge, Integer endAge, String breed, String size, Boolean filterPrefs, Integer userId) {
        List<Pet> allPets = petRepository.findAll();
        allPets = allPets.stream()
            .filter(pet -> petType == null                  ||  pet.getPetType().equalsIgnoreCase(petType))
            .filter(pet -> gender == null                   ||  String.valueOf(pet.getGender()).equalsIgnoreCase(gender))
            .filter(pet -> startAge == null                 ||  pet.getAge() >= startAge)
            .filter(pet -> endAge == null                   ||  pet.getAge() <= endAge)
            .filter(pet -> breed == null || breed.isBlank() ||  pet.getBreed().toLowerCase().contains(breed.toLowerCase()))
            .filter(pet -> size == null                     ||  pet.getSize().equalsIgnoreCase(size)).toList();

        if (allPets.isEmpty()) {
            throw new ResourceNotFoundException("Pet", petType + gender + startAge + endAge + breed + size);
        }

        if (filterPrefs == null || !filterPrefs){
            return allPets.stream()
                .map(PetDTO::fromEntity)
                .toList();
        }

        UserDTO authUserDTO;
        try {
            // user exists, try to filter
            authUserDTO = userService.findById(userId);
        } catch (Exception e) {
            // else we just return what we have
            return allPets.stream()
                .map(PetDTO::fromEntity)
                .toList();
        }

        List<UserPreferences> prefs = userPrefService.findAllByUserId(authUserDTO.id());
        return allPets.stream()
            .map(pet -> Map.entry(pet, scorePet(pet, prefs))) // pair pet + score
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue())) // high -> low
            .map(entry -> PetDTO.fromEntity(entry.getKey()))
            .toList();
    }

    @Transactional
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
            .collect(Collectors.toMap(this::buildKey, p -> p, (existing, replacement) -> {
                System.out.println("Duplicate found for key: " + buildKey(existing));
                return existing;
            }));

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

    // Build a composite key using attributes that, collectively, should never be repeated
    // by the law of statistics (I guess?)
    private String buildKey(Pet pet) {
        return String.join("__",
            pet.getSite().getSiteId().toString().trim(),
            pet.getName().toLowerCase().trim(),
            pet.getBreed().toLowerCase().trim(),
            pet.getPetType().toLowerCase().trim(),
            String.valueOf(pet.getGender()).toLowerCase().trim()
        ).replaceAll("\\s+", " ");
    }

    // If location, price, or status has changes between the new scrape and the old DB,
    // update the DB entry
    private boolean hasChanges(Pet existing, Pet scraped) {
        return !existing.getLocation().equals(scraped.getLocation())
            || existing.getPrice() != scraped.getPrice()
            || !existing.getPetStatus().equals(scraped.getPetStatus())
            || existing.getAge() != scraped.getAge()
            || existing.getSecondaryImages().size() != scraped.getSecondaryImages().size();
    }

    // Only update scraper-owned fields
    private void applyUpdates(Pet existing, Pet scraped) {
        existing.setLocation(scraped.getLocation());
        existing.setPrice(scraped.getPrice());
        existing.setPetStatus(scraped.getPetStatus());
        existing.setAge(scraped.getAge());

        for (PetImage image : scraped.getSecondaryImages()) 
            image.setPet(existing);
        
        existing.setSecondaryImages(scraped.getSecondaryImages());
        for (PetImage petImage : existing.getSecondaryImages()) {
            petImage.setPet(existing);
        }
    }
}
