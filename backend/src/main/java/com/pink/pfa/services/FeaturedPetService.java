package com.pink.pfa.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pink.pfa.controllers.requests.FeaturedPetRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.FeaturedPets;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.datatransfer.FeaturedPetDTO;
import com.pink.pfa.repos.FeaturedPetsRepository;
import com.pink.pfa.repos.PetRepository;


/**
 * PetService<br>
 * <br>
 * Central service layer for pet-related business logic.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Read pets from the database via {@link PetRepository}.</li>
 *   <li>Convert {@link Pet} entities to {@link FeaturedPetDTO} objects to format for frontend
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
public class FeaturedPetService {
    private static final Logger log = LoggerFactory.getLogger(PetService.class);
    private final FeaturedPetsRepository featuredPetRepository;
    private final PetRepository petRepository;

    public FeaturedPetService (FeaturedPetsRepository featuredPetRepository, PetRepository petRepository) {
        this.featuredPetRepository = featuredPetRepository;
        this.petRepository = petRepository;
    }

    /**
     * Returns all pets as a list of {@link FeaturedPetDTO}s by fetching entities from the database and mapping
     * each {@link Pet} to a DTO to avoid exposing sensitive fields.
     *
     * @return list of {@link FeaturedPetDTO}
     */
    public List<FeaturedPetDTO> findAll() {
        return featuredPetRepository.findAll()
            .stream()
            .map(FeaturedPetDTO::fromEntity)
            .toList();
    }

    public FeaturedPetDTO addFPet(FeaturedPetRequest request) {
        FeaturedPets fPet = new FeaturedPets ();

        fPet.setPet(petRepository.findById(request.petId())
            .orElseThrow(() -> new ResourceNotFoundException("Pet", request.petId()))
        );

        FeaturedPets savedFPet = featuredPetRepository.save(fPet);
        return FeaturedPetDTO.fromEntity(savedFPet);
    }

    public FeaturedPetDTO addFRandomPetByType(String type) {
        FeaturedPets fPet = new FeaturedPets();

        fPet.setPet(petRepository.findByPetStatusNotAndTypeRand(type, "INACTIVE")
            .orElseThrow(() -> new ResourceNotFoundException("No active pets found", 0))
        );

        FeaturedPets savedFPet = featuredPetRepository.save(fPet);
        return FeaturedPetDTO.fromEntity(savedFPet);
    }

    public List<FeaturedPetDTO> setupFeaturedByCount(int dogCount, int catCount) {
        featuredPetRepository.findAll()
            .forEach(fPet -> featuredPetRepository.delete(fPet));

        List<FeaturedPetDTO> newlyAdded = new ArrayList<>();

        for (int i = 0; i < dogCount; i++) {
            newlyAdded.add(addFRandomPetByType("Dog"));
        }
        for (int i = 0; i < catCount; i++) {
            newlyAdded.add(addFRandomPetByType("Cat"));
        }

        return newlyAdded;
    }

    /**
     * Fetches a single pet by ID and returns it as a {@link FeaturedPetDTO}.
     * Throws an exception if the pet does not exist.
     *
     * @param id database ID of the pet
     * @return {@link FeaturedPetDTO} for the requested pet
     */    
    public FeaturedPetDTO findByPetId(Integer id) {
        return featuredPetRepository.findById(id)
            .map(FeaturedPetDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
    }

    public void deleteByPetId(Integer petId) {
        featuredPetRepository.deleteByPet_PetId(petId);
    }
}
