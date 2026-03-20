package com.pink.pfa.services;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.controllers.requests.NewAdoptionSiteRequest;
import com.pink.pfa.models.datatransfer.AdoptionSiteDTO;
import com.pink.pfa.repos.AdoptionSiteRepository;

@Service
@RequestMapping
public class AdoptionSiteService {

    private final AdoptionSiteRepository adoptionSiteRepository;

    public AdoptionSiteService (AdoptionSiteRepository adoptionSiteRepository) {
        this.adoptionSiteRepository = adoptionSiteRepository;
    }

    // submit a site for acceptance
    public AdoptionSiteDTO submitNewSite(NewAdoptionSiteRequest request) {
        return null;
    }
    

    public AdoptionSiteDTO approveNewSiteRequest(int id) {
        return null;
    }
    
    public AdoptionSiteDTO denyNewSiteRequest(int id) {
        return null; 
    }
}
