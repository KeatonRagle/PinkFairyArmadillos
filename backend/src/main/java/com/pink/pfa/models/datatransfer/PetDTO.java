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
        int compatibility_score,
        String img_url
) {

    public static PetDTO fromEntity(Pet pet) {
        return new PetDTO(
                pet.getPetId(),
                pet.getName(),
                pet.getBreed(), 
                pet.getAge(), 
                pet.getGender(), 
                pet.getPetType(), 
                pet.getLocation(), 
                pet.getPrice(), 
                pet.getPetStatus(), 
                pet.getCompatibilityScore(),
                pet.getImgUrl()
                );
    }
}
