package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserPasswordRequest(
    @NotBlank
    String password
) {}
