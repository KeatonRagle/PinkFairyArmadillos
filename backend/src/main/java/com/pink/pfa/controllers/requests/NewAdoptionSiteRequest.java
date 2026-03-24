package com.pink.pfa.controllers.requests;

public record NewAdoptionSiteRequest(
    String url,
    String name,
    String email,
    String phone
) {}
