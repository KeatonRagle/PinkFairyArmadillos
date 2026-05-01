package com.pink.pfa.controllers.requests;
import jakarta.validation.constraints.NotNull;

public record FeaturedPetRequest(
    @NotNull
    int petId
) {}