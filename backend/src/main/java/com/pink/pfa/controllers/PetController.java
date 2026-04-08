package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.datatransfer.PetDTO;
import com.pink.pfa.services.PetService;

/**
 * REST controller exposing the {@code /api/pets} API surface for the Pets for All platform.
 *
 * <p>This controller serves as the primary entry point for pet data interactions,
 * handling both database-backed retrieval and on-demand web scraping from external
 * adoption sites. It is intended to be consumed by the React/Vite frontend or any
 * authorized API client.
 *
 * <p>Use this controller when you need to:
 * <ul>
 *   <li>Fetch all available pet listings stored in the database</li>
 *   <li>Look up a specific pet record by its unique ID</li>
 *   <li>Trigger a live scrape of external adoption sites to populate or refresh pet data</li>
 * </ul>
 *
 * <p>All business logic is delegated to {@link PetService}; this controller is
 * responsible only for request mapping, response shaping, and timestamp injection.
 */
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController (PetService petService) {
        this.petService = petService;
    }
 

    /**
     * Returns all pets currently stored in the database, along with a UTC timestamp.
     *
     * @return a map containing a {@code Pets} list and a {@code Timestamp} string
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<PetDTO>> getAllPets() {
        try {
            return ResponseEntity.ok().body(petService.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getFiltered")
    public ResponseEntity<List<PetDTO>> getFilteredPets(
        @RequestParam(required = false) String petType,
        @RequestParam(required = false) String gender,
        @RequestParam(required = false) Integer startAge,
        @RequestParam(required = false) Integer endAge,
        @RequestParam(required = false) String breed,
        @RequestParam(required = false) String size
    ) {
        try {
            return ResponseEntity.ok().body(petService.findByFilter(petType, gender, startAge, endAge, breed, size));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

 
    /**
     * Returns a single pet by its ID, along with a UTC timestamp.
     *
     * @param id the unique identifier of the pet
     * @return a map containing the matched {@code Pet} and a {@code Timestamp} string
     */
    @GetMapping("/{id}")
    public ResponseEntity<PetDTO> getPetById(
        @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok().body(petService.findById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
