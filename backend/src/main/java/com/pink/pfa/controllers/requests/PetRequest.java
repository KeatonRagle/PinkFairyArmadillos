package com.pink.pfa.controllers.requests;
import jakarta.validation.constraints.NotNull;

public record PetRequest(
    @NotNull
    int siteId,
    String name,
    String breed,
    int age,
    char gender,
    String petType,
    String location,
    double price,
    String size,
    String petStatus,
    int compatibilityScore,
    String imgUrl
) {}