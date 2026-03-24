package com.pink.pfa.models.datatransfer;

import com.pink.pfa.models.Pet;


/**
 * Data Transfer Object representing a {@link Pet} exposed across the API boundary.
 * <p>
 * Used to decouple the internal {@link Pet} entity from the API response shape,
 * ensuring only intended fields are serialized and returned to the client.
 */
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
    /**
     * Maps a {@link Pet} entity to a {@link PetDTO}.
     *
     * @param pet the entity to convert
     * @return a {@link PetDTO} populated with the entity's data
     */
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
                pet.getCompatibilityScore()
                );
    }
}
