package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostRequest(
    @NotNull
    Integer userID,
    @NotBlank
    String comment
) {}

