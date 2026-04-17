package com.pink.pfa.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.FeaturedPetRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.SiteAlreadyExistsException;
import com.pink.pfa.models.datatransfer.FeaturedPetDTO;
import com.pink.pfa.services.FeaturedPetService;
import com.pink.pfa.services.PetService;

import jakarta.transaction.Transactional;

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
@RequestMapping("/api/featuredPets")
public class FeaturePetController {

    private final FeaturedPetService featuredPetService;

    public FeaturePetController (FeaturedPetService featuredPetService) {
        this.featuredPetService = featuredPetService;
    }

    /**
     * Returns all pets currently stored in the database, along with a UTC timestamp.
     *
     * @return a map containing a {@code Pets} list and a {@code Timestamp} string
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<FeaturedPetDTO>> getAllFeaturedPets() {
        try {
            return ResponseEntity.ok().body(featuredPetService.findAll());
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
    public ResponseEntity<FeaturedPetDTO> getFeaturedPetById(
        @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok().body(featuredPetService.findById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Annotated as such to rollback on failure
    @Transactional
    @GetMapping("/setupNextFeatureds")
    public ResponseEntity<List<FeaturedPetDTO>> setupNextFeatureds(
        @RequestParam Integer dogCount,
        @RequestParam Integer catCount
    ) {
        featuredPetService.findAll()
            .forEach(fPet -> featuredPetService.deleteByPetId(fPet.petId()));

        List<FeaturedPetDTO> newlyAdded = new ArrayList<>();

        try {
            for (int i = 0; i < dogCount; i++) {
                newlyAdded.add(featuredPetService.addFRandomPetByType("Dog", "Chosen randomly"));
            }
            for (int i = 0; i < catCount; i++) {
                newlyAdded.add(featuredPetService.addFRandomPetByType("Cat", "Chosen randomly"));
            }
            return ResponseEntity.ok().body(newlyAdded);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/addPet")
    public ResponseEntity<FeaturedPetDTO> addFeaturedPet(
        @RequestBody FeaturedPetRequest petReq
    ) {
        try { 
            return ResponseEntity.ok().body(featuredPetService.addFPet(petReq));
        } catch (SiteAlreadyExistsException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
