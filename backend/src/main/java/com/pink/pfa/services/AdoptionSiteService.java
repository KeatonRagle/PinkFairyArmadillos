package com.pink.pfa.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.controllers.requests.NewAdoptionSiteRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.datatransfer.AdoptionSiteDTO;
import com.pink.pfa.repos.AdoptionSiteRepository;

import jakarta.transaction.Transactional;

@Service
@RequestMapping
public class AdoptionSiteService {

    private final AdoptionSiteRepository adoptionSiteRepository;

    public AdoptionSiteService (AdoptionSiteRepository adoptionSiteRepository) {
        this.adoptionSiteRepository = adoptionSiteRepository;
    }

    public List<AdoptionSite> findAll() {
        return adoptionSiteRepository.findAll();
    }

    // submit a site for acceptance
    public AdoptionSiteDTO submitNewSite(NewAdoptionSiteRequest request) {
        AdoptionSite site = new AdoptionSite();
        site.setUrl(request.url());
        site.setName(request.name());
        site.setEmail(request.email());
        site.setPhone(request.phone());

        AdoptionSite savedSite = adoptionSiteRepository.save(site);

        return AdoptionSiteDTO.fromEntity(savedSite);
    }
    
 
    @Transactional
    public void approveNewSiteRequest(int id) {
        AdoptionSite site = adoptionSiteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AdoptionSite", id));

        site.setStatus('A');
    }
    
    @Transactional
    public void denyNewSiteRequest(int id) {
        AdoptionSite site = adoptionSiteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AdoptionSite", id));
        
        site.setStatus('D');
    }
}
