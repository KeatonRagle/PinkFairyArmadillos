package com.pink.pfa.models.datatransfer;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pink.pfa.models.Pet;


/**
 * Data Transfer Object representing a pet scraped from an external adoption site.
 * <p>
 * This record is used as an intermediate deserialization target when processing
 * raw scraped data before it is mapped to a {@link Pet} entity for persistence.
 * <p>
 * JSON field names use Pascal case (e.g., {@code "Type"}, {@code "Breed"}) to match
 * the format returned by the scraping source. All fields are nullable — {@link #toEntity()}
 * applies safe defaults for any missing values.
 */
public record ScrapedPetDTO(
    @JsonProperty("Type")    String type,
    @JsonProperty("Breed")   String breed,
    @JsonProperty("Gender")  String gender,
    @JsonProperty("Image")   String image,
    @JsonProperty("Name")    String name,
    @JsonProperty("Size")    String size,
    @JsonProperty("Age")     Integer age,
    @JsonProperty("Price")   Double price
) {
    private static final ObjectMapper mapper = new ObjectMapper();


    /**
     * Converts a raw key-value map from a scrape result into a {@link ScrapedPetDTO}.
     *
     * @param data map of field names to values
     * @return deserialized {@link ScrapedPetDTO}
     */
    public static ScrapedPetDTO fromMap(Map<String, Object> data) {
        return mapper.convertValue(data, ScrapedPetDTO.class);
    }


    /**
     * Maps this DTO to a {@link Pet} entity for persistence.
     * Null or empty fields fall back to safe defaults ({@code "Unknown"}, {@code 0}, etc.).
     *
     * @return a {@link Pet} entity populated with the scraped data
     */
    public Pet toEntity() {
        Pet pet = new Pet();
        pet.setName(name != null ? name : "Unknown");
        pet.setBreed(breed != null ? breed : "Unknown");
        pet.setPetType(type != null ? type : "Unknown");
        pet.setGender(gender != null && !gender.isEmpty() ? gender.charAt(0) : 'U');
        pet.setImgUrl(image != null ? image : "");
        pet.setAge(age != null ? age : 0);
        pet.setPrice(price != null ? price : 0.0);
        pet.setLocation("Unknown");
        pet.setPetStatus("Available");
        pet.setSize(size != null ? size : "Unknown");
        return pet;
    }
}
