package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.NotBlank;

public record CommentRequests(
    @NotBlank
    String url,
    String name,
    String email,
    String phone,
    Double rating
) {}

