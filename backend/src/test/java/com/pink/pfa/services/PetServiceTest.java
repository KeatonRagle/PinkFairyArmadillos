package com.pink.pfa.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.context.PfaBase;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.datatransfer.PetDTO;
import com.pink.pfa.repos.AdoptionSiteRepository;
import com.pink.pfa.repos.PetRepository;

/**
 * Integration test suite for {@link PetService}.
 *
 * <p>Spins up a full Spring context backed by a Testcontainers MySQL instance and
 * seeds it with known pets via {@link TestDataConfig} (Buddy, Luna, Max). Tests
 * verify correct DTO mapping, status diversity, and exception behavior for invalid
 * lookups.
 */

@ExtendWith(MockitoExtension.class)
class PetServiceTest extends PfaBase {

    private final PetService petService;
    private final PetRepository petRepository;
    private final AdoptionSiteRepository adoptionSiteRepository;

    @Mock
    private final WebScraperService webScraperService;

    @Autowired
    public PetServiceTest(PetService petService, 
        PetRepository petRepository,
        WebScraperService webScraperService,
        AdoptionSiteRepository adoptionSiteRepository
    ) {
        this.petService = petService;
        this.petRepository = petRepository;
        this.webScraperService = webScraperService;
        this.adoptionSiteRepository = adoptionSiteRepository;
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    /**
     * Verifies that findAll returns all three pets seeded by TestDataConfig.
     */
    @Test
    void findAll_ShouldReturnAllSeededPets() {
        List<PetDTO> pets = petService.findAll();
        assertTrue(pets.size() >= 3, "Expected at least 3 seeded pets, got: " + pets.size());
    }

    /**
     * Verifies that findAll returns PetDTOs with non-null required fields,
     * confirming that entity-to-DTO mapping is working correctly.
     */
    @Test
    void findAll_ShouldReturnPopulatedDTOs() {
        List<PetDTO> pets = petService.findAll();

        pets.forEach(pet -> {
            assertNotNull(pet.name(), "Pet name should not be null");
            assertNotNull(pet.pet_type(), "Pet species should not be null");
            assertNotNull(pet.pet_status(), "Pet status should not be null");
        });
    }

    /**
     * Verifies that both "available" and "pending" statuses are present in the
     * results, confirming that no status filtering is applied by default.
     * TestDataConfig seeds Buddy and Luna as "available" and Max as "pending".
     */
    @Test
    void findAll_ShouldIncludePetsOfDifferentStatuses() {
        List<PetDTO> pets = petService.findAll();

        boolean hasAvailable = pets.stream().anyMatch(p -> "available".equals(p.pet_status()));
        boolean hasPending   = pets.stream().anyMatch(p -> "pending".equals(p.pet_status()));

        assertTrue(hasAvailable, "Expected at least one 'available' pet");
        assertTrue(hasPending,   "Expected at least one 'pending' pet");
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    /**
     * Verifies that a pet can be fetched by ID and the returned DTO matches
     * the expected name.
     */
    @Test
    void findById_WithValidId_ShouldReturnCorrectPet() {
        // Fetch Buddy's ID from the repo since IDs are auto-generated
        Integer buddyId = petRepository.findAll()
                .stream()
                .filter(p -> "Buddy".equals(p.getName()))
                .findFirst()
                .orElseThrow()
                .getPetId();

        PetDTO result = petService.findById(buddyId);

        assertEquals("Buddy", result.name());
        assertEquals("dog", result.pet_type());
    }

    /**
     * Verifies that fetching a pet with a nonexistent ID throws an exception.
     */
    @Test
    void findById_WithInvalidId_ShouldThrowException() {
        assertThrows(Exception.class, () -> petService.findById(999999));
    }

    /*----------------------------------*\
    || ******************************** ||      
    || **********SYNC TESTING********** ||
    || ******************************** ||                                          
    *///-------------------------------\\\*

    @Test
    void trySync_threePets_oneDupe() {
        List<String> sites =  List.of("https://hsdallascounty.org");

        // Mock some data to avoid scraping for real
        List<Pet> mockData = List.of(
            // This pet is already seeded
            new Pet("Buddy", "Labrador Retriever", 3, 'M', "dog", "Austin, TX", 150.0, "available", 85),
            // ...while these are new
            new Pet("Mulch", "Toy Poodle", 2, 'F', "dog", "Austin, TX", 150.0, "available", 85),
            new Pet("Pibble", "Pit Bull", 1, 'M', "dog", "Austin, TX", 150.0, "available", 85)
        );

        // ...and set their site to the one the other seeded animals use
        mockData.forEach(p -> p.setSite(
            adoptionSiteRepository.findBySiteId(1)
            .orElseThrow(() -> new IllegalStateException("There must be one Adoption Site seeded"))
        ));

        when(webScraperService.runScraper(sites)).thenReturn(mockData);  
        List<Pet> testScrape = webScraperService.runScraper(sites);

        // Sync data, and lets test to make sure it worked
        petService.sync(testScrape);

        // We should have three elements
        assertEquals(testScrape.size(), 3);
        //...and all should be in our database
        for (Pet p : testScrape) {
             assertTrue(petService.findByName(p.getName()) != null, "Expected at least one pet");
        }

        // Lets filter for all the inactive pets
        List<PetDTO> inactives = petRepository.findAll()
            .stream()
            .filter(p -> "INACTIVE".equals(p.getPetStatus()))
            .map(PetDTO::fromEntity)
            .toList();
        
        // We had two seeded pets not in our scrape, so they should be deactivated for now
        assertEquals(inactives.size(), 2);
    }
}
