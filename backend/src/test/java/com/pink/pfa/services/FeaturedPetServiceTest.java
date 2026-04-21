package com.pink.pfa.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.controllers.requests.FeaturedPetRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.datatransfer.FeaturedPetDTO;
import com.pink.pfa.repos.FeaturedPetsRepository;

/**
 * Integration test suite for {@link FeaturedPetService}.
 *
 * <p>Spins up a full Spring context backed by a Testcontainers MySQL instance and
 * seeds it with known pets via {@link com.pink.pfa.config.TestDataConfig}.
 * Tests verify correct featured pet creation, retrieval, deletion, and exception
 * behavior for invalid lookups.
 */
@ExtendWith(MockitoExtension.class)
class FeaturedPetServiceTest extends PfaBase {

    private final FeaturedPetService featuredPetService;
    private final FeaturedPetsRepository featuredPetsRepository;

    @Autowired
    public FeaturedPetServiceTest(
        FeaturedPetService featuredPetService,
        FeaturedPetsRepository featuredPetsRepository
    ) {
        this.featuredPetService = featuredPetService;
        this.featuredPetsRepository = featuredPetsRepository;
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    /**
     * Verifies that findAll returns a non-null list.
     */
    @Test
    @Transactional
    void findAll_ShouldReturnNonNullList() {
        List<FeaturedPetDTO> result = featuredPetService.findAll();

        assertNotNull(result, "findAll should never return null");
    }

    /**
     * Verifies that findAll grows by one after a featured pet is added.
     */
    @Test
    @Transactional
    void findAll_ShouldReflectNewlyAddedFeaturedPet() {
        Pet pet = getRandPet();
        featuredPetService.addFPet(new FeaturedPetRequest(pet.getPetId(), "Test reason"));

        List<FeaturedPetDTO> result = featuredPetService.findAll();

        assertTrue(result.size() >= 1,
            "Expected at least one featured pet after adding one");
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    /**
     * Verifies that findById returns the correct featured pet when given a valid ID.
     */
    @Test
    @Transactional
    void findById_WithValidId_ShouldReturnCorrectFeaturedPet() {
        Pet pet = getRandPet();
        FeaturedPetDTO created = featuredPetService.addFPet(
            new FeaturedPetRequest(pet.getPetId(), "Test reason")
        );

        FeaturedPetDTO found = featuredPetService.findByPetId(created.id());

        assertNotNull(found, "Expected a featured pet to be returned");
        assertEquals(created.petId(), found.petId(), "Returned ID should match the created one");
    }

    /**
     * Verifies that findById throws ResourceNotFoundException for a nonexistent ID.
     */
    @Test
    @Transactional
    void findById_WithInvalidId_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
            () -> featuredPetService.findByPetId(999999),
            "Expected ResourceNotFoundException for unknown ID");
    }

    // -------------------------------------------------------------------------
    // addFPet
    // -------------------------------------------------------------------------

    /**
     * Verifies that addFPet persists a new featured pet linked to the correct pet.
     */
    @Test
    @Transactional
    void addFPet_ShouldPersistAndReturnFeaturedPet() {
        Pet pet = getRandPet();
        FeaturedPetRequest request = new FeaturedPetRequest(pet.getPetId(), "Chosen manually");

        FeaturedPetDTO result = featuredPetService.addFPet(request);

        assertNotNull(result, "Result should not be null");
        assertNotNull(petRepository.findById(result.petId()), "Saved featured pet should have a generated ID");
        assertEquals(pet.getPetId(), result.petId(),
            "Featured pet should be linked to the correct pet");
    }

    /**
     * Verifies that addFPet throws ResourceNotFoundException for an unknown pet ID.
     */
    @Test
    @Transactional
    void addFPet_ShouldThrowForUnknownPetId() {
        FeaturedPetRequest request = new FeaturedPetRequest(999999, "Broken by design");

        assertThrows(ResourceNotFoundException.class,
            () -> featuredPetService.addFPet(request),
            "Expected exception when pet does not exist");
    }

    // -------------------------------------------------------------------------
    // addFRandomPetByType
    // -------------------------------------------------------------------------

    /**
     * Verifies that addFRandomPetByType returns a featured pet of the correct type.
     */
    @Test
    @Transactional
    void addFRandomPetByType_ShouldReturnFeaturedPetOfCorrectType() {
        FeaturedPetDTO result = featuredPetService.addFRandomPetByType("Dog", "Chosen randomly");

        assertNotNull(result, "Result should not be null");
        assertEquals("Dog", petService.findById(result.petId()).pet_type(),
            "Featured pet should be of the requested type");
    }

    /**
     * Verifies that addFRandomPetByType throws ResourceNotFoundException
     * when no active pets of the given type exist.
     */
    @Test
    @Transactional
    void addFRandomPetByType_ShouldThrowWhenNoActivePetsOfTypeExist() {
        assertThrows(ResourceNotFoundException.class,
            () -> featuredPetService.addFRandomPetByType("Dragon", "No such type"),
            "Expected exception when no active pets of given type exist");
    }

    // -------------------------------------------------------------------------
    // deleteByPetId
    // -------------------------------------------------------------------------

    /**
     * Verifies that deleteByPetId removes the featured pet so it no longer appears
     * in subsequent queries.
     */
    @Test
    @Transactional
    void deleteByPetId_ShouldRemoveFeaturedPet() {
        Pet pet = getRandPet();
        FeaturedPetDTO fPet = featuredPetService.addFPet(new FeaturedPetRequest(pet.getPetId(), "To be deleted"));

        int size = featuredPetService.findAll().size();
        featuredPetService.deleteByPetId(fPet.id());
        assertTrue(size > featuredPetService.findAll().size(),
            "Featured pet should no longer exist after deletion");
    }

    /**
     * Verifies that deleteByPetId does not throw when the pet ID has no featured entry.
     */
    @Test
    @Transactional
    void deleteByPetId_ShouldNotThrowForUnknownPetId() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
            () -> featuredPetService.deleteByPetId(999999),
            "Deleting a non-existent featured pet should not throw"
        );
    }
}