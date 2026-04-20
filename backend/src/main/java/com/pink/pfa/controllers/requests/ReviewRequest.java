package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
    @NotNull
    Integer userID,
    @NotNull
    Integer siteId,
    @NotBlank
    Double rating,
    @NotBlank
    String comment
) {}

