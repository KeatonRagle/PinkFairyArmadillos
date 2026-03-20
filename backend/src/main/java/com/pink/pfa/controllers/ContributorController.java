package com.pink.pfa.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.NewAdoptionSiteRequest;
import com.pink.pfa.services.AdoptionSiteService;

@RestController
@RequestMapping("/api/contributor")
public class ContributorController {

    private final AdoptionSiteService adoptionSiteService;

    public ContributorController (AdoptionSiteService adoptionSiteService) {
        this.adoptionSiteService = adoptionSiteService;
    }

    
    @PostMapping("/submitSite")
    public ResponseEntity<?> submitNewSite(@RequestBody NewAdoptionSiteRequest request) {
        return null;
    }
    
}
