package com.pink.pfa.controllers;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.services.PetService;

@RestController
@RequestMapping("/api/pets")
public class Petcontroller {

    @Autowired
    private PetService petService;

    @GetMapping("/getAll")
    public Map<String, Object> getAllPets() {
        return Map.of(
            "Pets: ", petService.findAll(),
            "Timestamp: ", Instant.now().toString()
        );
    }


    @GetMapping("/{id}")
    public Map<String, Object> getPetById(
        @PathVariable Integer id
    ) {
        return Map.of(
            "Pet: ", petService.findById(id),
            "Timestamp: ", Instant.now().toString()
        );
    }

}
