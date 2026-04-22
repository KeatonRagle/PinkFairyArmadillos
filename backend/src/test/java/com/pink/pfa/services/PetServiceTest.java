package com.pink.pfa.services;

import java.time.LocalDate;
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
import org.springframework.transaction.annotation.Transactional;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.context.PfaBase;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.User;
import com.pink.pfa.models.UserPreferences;
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

    @Mock
    private UserService userService;

    @Mock
    private final UserPrefService userPrefService;

    @Autowired
    public PetServiceTest(PetService petService, 
        PetRepository petRepository,
        WebScraperService webScraperService,
        AdoptionSiteRepository adoptionSiteRepository,
        UserPrefService userPrefService
    ) {
        this.petService = petService;
        this.petRepository = petRepository;
        this.webScraperService = webScraperService;
        this.adoptionSiteRepository = adoptionSiteRepository;
        this.userPrefService = userPrefService;
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    /**
     * Verifies that findAll returns all five pets seeded by TestDataConfig.
     */
    @Test
    @Transactional
    void findAll_ShouldReturnAllSeededPets() {
        List<PetDTO> pets = petService.findAll();
        assertTrue(pets.size() >= 5, "Expected at least 5 seeded pets, got: " + pets.size());
    }

    /**
     * Verifies that findAll returns PetDTOs with non-null required fields,
     * confirming that entity-to-DTO mapping is working correctly.
     */
    @Test
    @Transactional
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
    @Transactional
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
    @Transactional
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
        assertEquals("Dog", result.pet_type());
    }

    /**
     * Verifies that fetching a pet with a nonexistent ID throws an exception.
     */
    @Test
    @Transactional
    void findById_WithInvalidId_ShouldThrowException() {
        assertThrows(Exception.class, () -> petService.findById(999999));
    }

    // -------------------------------------------------------------------------
    // findByFIlter
    // -------------------------------------------------------------------------

    /**
     * Verifies that findByFilter returns all three dogs and all two cats seeded by TestDataConfig.
     */
    @Test
    @Transactional
    void findByFilter_ShouldReturnPetsByPetType() {
        List<PetDTO> dogs = petService.findByFilter("Dog", null, null, null, null, null, null, null);
        assertTrue(dogs.size() >= 3, "Expected at least 3 seeded pets, got: " + dogs.size());

        List<PetDTO> cats = petService.findByFilter("Cat", null, null, null, null, null, null, null);
        assertTrue(cats.size() >= 2, "Expected at least 2 seeded pets, got: " + cats.size());
    }

    /**
     * Verifies that findByFilter returns all three male pets and all two female pets seeded by TestDataConfig.
     */
    @Test
    @Transactional
    void findByFilter_ShouldReturnPetsByGender() {
        List<PetDTO> malePets = petService.findByFilter(null, "M", null, null, null, null, null, null);
        assertTrue(malePets.size() >= 3, "Expected at least 3 seeded pets, got: " + malePets.size());

        List<PetDTO> femalePets = petService.findByFilter(null, "F", null, null, null, null, null, null);
        assertTrue(femalePets.size() >= 2, "Expected at least 2 seeded pets, got: " + femalePets.size());
    }

    /**
     * Verifies that findByFilter returns pets within correct age ranges by TestDataConfig.
     */
    @Test
    @Transactional
    void findByFilter_ShouldReturnPetsByAge() {
        List<PetDTO> youngPets = petService.findByFilter(null, null, null, 40, null, null, null, null);
        assertTrue(youngPets.size() >= 3, "Expected at least 3 seeded pets, got: " + youngPets.size());

        List<PetDTO> middlePets = petService.findByFilter(null, null, 20, 50, null, null, null, null);
        assertTrue(middlePets.size() >= 3, "Expected at least 3 seeded pets, got: " + middlePets.size());

        List<PetDTO> oldPets = petService.findByFilter(null, null, 30, null, null, null, null, null);
        assertTrue(oldPets.size() >= 3, "Expected at least 3 seeded pets, got: " + oldPets.size());
    }

    /**
     * Verifies that findByFilter returns all one golden retrievers and all two domestic shorthairs seeded by TestDataConfig.
     */
    @Test
    @Transactional
    void findByFilter_ShouldReturnPetsByBreed() {
        List<PetDTO> goldenRetrievers = petService.findByFilter(null, null, null, null, "Golden Retriever", null, null, null);
        assertTrue(goldenRetrievers.size() >= 1, "Expected at least 1 seeded pets, got: " + goldenRetrievers.size());

        List<PetDTO> domesticShorthairs = petService.findByFilter(null, null, null, null, "Domestic Shorthair", null, null, null);
        assertTrue(domesticShorthairs.size() >= 2, "Expected at least 2 seeded pets, got: " + domesticShorthairs.size());
    }

    /**
     * Verifies that findByFilter returns all two medium sized pets and all two large sized pets seeded by TestDataConfig.
     */
    @Test
    @Transactional
    void findByFilter_ShouldReturnPetsBySize() {
        List<PetDTO> mediumPets = petService.findByFilter(null, null, null, null, null, "Medium", null, null);
        assertTrue(mediumPets.size() >= 2, "Expected at least 2 seeded pets, got: " + mediumPets.size());

        List<PetDTO> largePets = petService.findByFilter(null, null, null, null, null, "Large", null, null);
        assertTrue(largePets.size() >= 2, "Expected at least 2 seeded pets, got: " + largePets.size());
    }

    /**
     * Verifies that findByFilter returns correct number of pets under certain mixed filter conditions TestDataConfig.
     */
    @Test
    @Transactional
    void findByFilter_ShouldReturnPetsByMixedFilters() {
        List<PetDTO> youngCats = petService.findByFilter("Cat", null, null, 20, null, null, null, null);
        assertTrue(youngCats.size() >= 1, "Expected at least 1 seeded pets, got: " + youngCats.size());

        List<PetDTO> mediumDogs = petService.findByFilter("Dog", null, null, null, null, "Medium", null, null);
        assertTrue(mediumDogs.size() >= 1, "Expected at least 1 seeded pets, got: " + mediumDogs.size());

        List<PetDTO> femaleDomesticShorthair = petService.findByFilter(null, "F", null, null, "Domestic Shorthair", null, null, null);
        assertTrue(femaleDomesticShorthair.size() >= 1, "Expected at least 1 seeded pets, got: " + femaleDomesticShorthair.size());
    }

    //* FILTER PREF TESTER */

    private int scoreTestHelper(PetDTO pet, List<UserPreferences> prefs) {
    int score = 0;

        for (UserPreferences pref : prefs) {
            String val = pref.getPrefValue();

            switch (pref.getPrefTrait()) {
                case BREED -> {
                    if (pet.breed().toLowerCase().contains(val.toLowerCase())) 
                        score++;
                }

                case PET_TYPE -> {
                    if (pet.pet_type().equalsIgnoreCase(val)) 
                        score++;
                }

                default -> {}
            }
        }

        return score;
    }

    @Test
    @Transactional
    void findByFilter_WithPrefs_ShouldSortResults() {
        // Mock authenticated user
        SeededUser mockUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        mockSecurityContext(mockUser.user());

        // Preferences: prefer SMALL dogs
        List<UserPreferences> prefs = List.of(
            new UserPreferences(UserPreferences.Preference.SIZE, "Small"),
            new UserPreferences(UserPreferences.Preference.PET_TYPE, "Dog")
        );

        List<PetDTO> result = petService.findByFilter(null, null, null, null, null, null, true, null);

        assertTrue(result.size() > 1);

        // First pet should match more preferences than last
        PetDTO first = result.get(0);
        PetDTO last  = result.get(result.size() - 1);

        int firstScore = scoreTestHelper(first, prefs);
        int lastScore  = scoreTestHelper(last, prefs);

        assertTrue(firstScore >= lastScore, "Expected sorted order by preference score");
    }

    @Test
    @Transactional
    void findByFilter_NoUser_ShouldFallbackToUnsorted() {
        List<PetDTO> result = petService.findByFilter(null, null, null, null, null, null, true, null);

        assertNotNull(result);
        assertTrue(!result.isEmpty());
    }

    /*----------------------------------*\
    || ******************************** ||      
    || **********SYNC TESTING********** ||
    || ******************************** ||                                          
    *///-------------------------------\\\*

    @Test
    @Transactional
    void trySync_threePets_oneDupe() {
        List<AdoptionSite> sites = List.of(new AdoptionSite("Dallas County", "", "", 0, "https://hsdallascounty.org", 'A', LocalDate.now()));

        // Mock some data to avoid scraping for real
        List<Pet> mockData = List.of(
            // This pet is already seeded, but the location is changed
            new Pet("Buddy", "Labrador Retriever", 24, 'M', "Dog", "Lubbock, TX", 150.0, "Medium", "available", 85, "placeholder", LocalDate.now()),
            // ...while these are new
            new Pet("Mulch", "Toy Poodle", 2, 'F', "dog", "Austin, TX", 150.0, "Small", "available", 85, "placeholder", LocalDate.now()),
            new Pet("Pibble", "Pit Bull", 1, 'M', "dog", "Austin, TX", 150.0, "Large", "available", 85, "placeholder", LocalDate.now())
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
        assertEquals(inactives.size(), 4);
    }

    @Test
    @Transactional
    void findByActive_ShouldReturnMoreThanOne() {
        List<PetDTO> activePets = petService.findAllActive();
        assertTrue(!activePets.isEmpty(), "Expected at least 1 seeded pets, got: " + activePets.size());
    }

    @Test
    @Transactional
    void findRandomActive_ShouldReturnMoreThanOne() {
        PetDTO randPet = petService.findRandomActivePetByType("Dog");
        assertTrue(randPet != null, "Expected a random seeded dog, got " + randPet.name());
    }
}
