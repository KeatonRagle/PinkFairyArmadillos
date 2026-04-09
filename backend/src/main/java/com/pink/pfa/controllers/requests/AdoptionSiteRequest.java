package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.NotBlank;

public record AdoptionSiteRequest(
    @NotBlank
    String url,
    String name,
    String email,
    String phone,
    Double rating,
    Integer userID
) {}
