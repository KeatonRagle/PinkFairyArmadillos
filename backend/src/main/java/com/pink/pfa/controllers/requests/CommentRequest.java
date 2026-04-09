package com.pink.pfa.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(
    @NotNull
    Integer userID,
    @NotNull
    Integer postID,
    @NotBlank
    String comment
) {}

