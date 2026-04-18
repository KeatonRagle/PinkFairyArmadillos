package com.pink.pfa.services;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pink.pfa.controllers.requests.FeaturedPetRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.FeaturedPets;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.datatransfer.FeaturedPetDTO;
import com.pink.pfa.models.datatransfer.PetDTO;
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
public class FeaturedPetService {
    private static final Logger log = LoggerFactory.getLogger(PetService.class);
    private final FeaturedPetsRepository featuredPetRepository;
    private final PetRepository petRepository;

    public FeaturedPetService (FeaturedPetsRepository featuredPetRepository, PetRepository petRepository) {
        this.featuredPetRepository = featuredPetRepository;
        this.petRepository = petRepository;
    }

    /**
     * Returns all pets as a list of {@link PetDTO}s by fetching entities from the database and mapping
     * each {@link Pet} to a DTO to avoid exposing sensitive fields.
     *
     * @return list of {@link PetDTO}
     */
    public List<FeaturedPetDTO> findAll() {
        return featuredPetRepository.findAll()
            .stream()
            .map(FeaturedPetDTO::fromEntity)
            .toList();
    }

    public FeaturedPetDTO addFPet(FeaturedPetRequest request) {
        FeaturedPets fPet = new FeaturedPets (LocalDate.now(), LocalDate.now().plusDays(1), request.reason());

        fPet.setPet(petRepository.findById(request.petId())
            .orElseThrow(() -> new ResourceNotFoundException("Pet", request.petId()))
        );

        FeaturedPets savedFPet = featuredPetRepository.save(fPet);
        return FeaturedPetDTO.fromEntity(savedFPet);
    }

    public FeaturedPetDTO addFRandomPetByType(String type, String reason) {
        FeaturedPets fPet = new FeaturedPets (LocalDate.now(), LocalDate.now().plusDays(1), reason);

        fPet.setPet(petRepository.findByPetStatusNotAndTypeRand(type, "INACTIVE")
            .orElseThrow(() -> new ResourceNotFoundException("No active pets found", 0))
        );

        FeaturedPets savedFPet = featuredPetRepository.save(fPet);
        return FeaturedPetDTO.fromEntity(savedFPet);
    }

    /**
     * Fetches a single pet by ID and returns it as a {@link PetDTO}.
     * Throws an exception if the pet does not exist.
     *
     * @param id database ID of the pet
     * @return {@link PetDTO} for the requested pet
     */    
    public FeaturedPetDTO findById(Integer id) {
        return featuredPetRepository.findById(id)
            .map(FeaturedPetDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
    }

    public void deleteByPetId(Integer petId) {
        featuredPetRepository.deleteByPet_PetId(petId);
    }
}
