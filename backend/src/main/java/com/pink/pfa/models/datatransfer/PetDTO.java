package com.pink.pfa.models.datatransfer;

import com.pink.pfa.models.Pet;

public record PetDTO(
        Integer id,
        String name,
        String breed,
        int age,
        char gender,
        String pet_type,
        String location,
        double price,
        String pet_status,
        int compatibility_score
) {

    public static PetDTO fromEntity(Pet pet) {
        return new PetDTO(
                pet.getPet_id(),
                pet.getName(),
                pet.getBreed(), 
                pet.getAge(), 
                pet.getGender(), 
                pet.getPet_type(), 
                pet.getLocation(), 
                pet.getPrice(), 
                pet.getPet_status(), 
                pet.getCompatability_score()
                );
    }
}
