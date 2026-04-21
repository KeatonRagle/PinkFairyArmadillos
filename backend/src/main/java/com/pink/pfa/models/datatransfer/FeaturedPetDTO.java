package com.pink.pfa.models.datatransfer;

import java.time.LocalDate;

import com.pink.pfa.models.FeaturedPets;
import com.pink.pfa.models.Pet;


/**
 * Data Transfer Object representing a {@link FeaturedPets} exposed across the API boundary.
 * <p>
 * Used to decouple the internal {@link FeaturedPets} entity from the API response shape,
 * ensuring only intended fields are serialized and returned to the client.
 */
public record FeaturedPetDTO(
        Integer id,
        Integer petId,
        LocalDate start,
        LocalDate end
) {
    /**
     * Maps a {@link Pet} entity to a {@link PetDTO}.
     *
     * @param pet the entity to convert
     * @return a {@link FeaturedPetDTO} populated with the entity's data
     */
    public static FeaturedPetDTO fromEntity(FeaturedPets pet) {
        return new FeaturedPetDTO(
            pet.getPetId(),
            pet.getPet().getPetId(),
            pet.getStartDate(), 
            pet.getEndDate() 
        );
    }
}
