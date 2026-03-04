package com.pink.pfa.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.models.datatransfer.PetDTO;
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
@RequestMapping("/api/pets")
public class PetService {

    @Autowired
    private PetRepository petRepository;


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
     * Throws an exception if the user does not exist.
     *
     * @param id database ID of the pet
     * @return {@link PetDTO} for the requested user
     */    
    public PetDTO findById(Integer id) {
        return petRepository.findById(id)
                .map(PetDTO::fromEntity)
                .orElseThrow(() -> new InvalidConfigurationPropertyValueException("Failed to Find ID", null, "Pet Not Found"));
    }
    
}
