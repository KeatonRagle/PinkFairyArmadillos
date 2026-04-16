package com.pink.pfa.controllers;

import org.junit.jupiter.api.Test;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.models.Pet;

public class PetControllerTest extends PfaBase {
    // -------------------------------------------------------------------------
    // getAllPets
    // -------------------------------------------------------------------------
    /**
     * Verifies that the getAllPets endpoint is unprotected by attempting to access
     * without sending any authentication
     * */
    @Test
    void getAllPets_ShouldBeUnProtected() {
        webTestClient.get().uri("/api/pets/getAll")
            .exchange()
            .expectStatus().isOk();
    }

    // -------------------------------------------------------------------------
    // getFilteredPets
    // -------------------------------------------------------------------------
    /**
     * Verifies that the getFilteredPets endpoint is unprotected by attempting to access
     * without sending any authentication
     * */
    @Test
    void getFilteredPets_ShouldBeUnProtected() {
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/pets/getFiltered")
                .build()
                )
            .exchange()
            .expectStatus().isOk();
    }

    /**
     * Verifies that the getFilteredPets endpoint returns a Ok (200) http code when 
     * some pet is found matching the given filter constraints
     * */
    @Test
    void getFilteredPets_WithFilterConstraintsMatchingSomePet_ShouldReturn200() {
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/pets/getFiltered")
                .queryParam("petType", "dog")
                .queryParam("gender", "m")
                .build()
                )
            .exchange()
            .expectStatus().isOk();
    }

    /**
     * Verifies that the getFilteredPets endpoint returns a NotFound (404) http code when 
     * no pets are found matching the given filter constraints
     * */
    @Test
    void getFilteredPets_WithFilterConstraintsNotMatchingAnyPet_ShouldReturn404() {
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/pets/getFiltered")
                .queryParam("breed", "some really random breed")
                .build()
                )
            .exchange()
            .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // getPetById
    // -------------------------------------------------------------------------
    /**
     * Verifies that the getPetById endpoint is unprotected by attempting to access
     * without sending any authentication
     * */
    @Test
    void getPetById_ShouldBeUnProtected() {
        Pet pet = getRandPet();
        webTestClient.get().uri("/api/pets/" + pet.getPetId())
            .exchange()
            .expectStatus().isOk();
    }

    /**
     * Verifies that the getPetById endpoint returns a Ok (200) Http code
     * upon valid Id input
     * */
    @Test
    void getPetById_WithValidPetId_ShouldReturn200() {
        Pet pet = getRandPet();
        webTestClient.get().uri("/api/pets/" + pet.getPetId())
                .exchange()
                .expectStatus().isOk();
    }
    
    /**
     * Verifies that the getPetById endpoint returns a NotFound (404) Http code
     * upon invalid Id input
     * */
    @Test
    void getPetById_WithInvalidPetId_ShouldReturn404() {
        webTestClient.get().uri("/api/pets/" + 99999)
                .exchange()
                .expectStatus().isNotFound();
    }
}
