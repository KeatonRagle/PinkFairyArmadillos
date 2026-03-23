package com.pink.pfa.models.datatransfer;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pink.pfa.models.Pet;

public record ScrapedPetDTO(
    @JsonProperty("Type")    String type,
    @JsonProperty("Breed")   String breed,
    @JsonProperty("Gender")  String gender,
    @JsonProperty("Image")   String image,
    @JsonProperty("Name")    String name
) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ScrapedPetDTO fromMap(Map<String, Object> data) {
        return mapper.convertValue(data, ScrapedPetDTO.class);
    }

    public Pet toEntity() {
        Pet pet = new Pet();
        pet.setName(name != null ? name : "Unknown");
        pet.setBreed(breed != null ? breed : "Unknown");
        pet.setPetType(type != null ? type : "Unknown");
        pet.setGender(gender != null && !gender.isEmpty() ? gender.charAt(0) : 'U');
        pet.setImgUrl(image != null ? image : "");
        pet.setAge(0);
        pet.setPrice(0.0);
        pet.setLocation("Unknown");
        pet.setPetStatus("Available");
        return pet;
    }
}
