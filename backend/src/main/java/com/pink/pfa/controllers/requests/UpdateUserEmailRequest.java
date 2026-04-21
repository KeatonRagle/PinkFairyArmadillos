package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserEmailRequest(
    @NotBlank
    String email
) {}
