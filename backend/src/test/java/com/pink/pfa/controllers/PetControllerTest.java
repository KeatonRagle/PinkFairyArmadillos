package com.pink.pfa.controllers;

import org.junit.jupiter.api.Test;

import com.pink.pfa.context.PfaBase;

public class PetControllerTest extends PfaBase {

    
    // -------------------------------------------------------------------------
    // getPetById
    // -------------------------------------------------------------------------
    @Test
    void getPetById_ShouldBeUnProtected() {
        webTestClient.get().uri("/api/pets/getPetById/1")
                .exchange()
                .expectStatus().isOk();
    }




}
