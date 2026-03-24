package com.pink.pfa.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.controllers.requests.NewAdoptionSiteRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.SiteAlreadyExistsException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.datatransfer.AdoptionSiteDTO;
import com.pink.pfa.repos.AdoptionSiteRepository;

import jakarta.transaction.Transactional;


/**
 * Service layer responsible for managing the full lifecycle of {@link AdoptionSite} entities.
 * <p>
 * This service acts as the primary business logic layer between the controller layer and the
 * {@link AdoptionSiteRepository}. It handles the submission, retrieval, approval, and denial
 * of adoption site requests submitted by contributors.
 * <p>
 * Adoption sites move through the following status lifecycle:
 * <ul>
 *   <li>{@code 'P'} — Pending: newly submitted, awaiting admin review</li>
 *   <li>{@code 'A'} — Approved: visible to the public</li>
 *   <li>{@code 'D'} — Denied: rejected by an admin</li>
 * </ul>
 * <p>
 * All database interactions are delegated to {@link AdoptionSiteRepository}.
 * Where applicable, {@link AdoptionSiteDTO} is used to prevent direct entity exposure
 * across the API boundary.
 */
@Service
@RequestMapping
public class AdoptionSiteService {

    private final AdoptionSiteRepository adoptionSiteRepository;

    public AdoptionSiteService (AdoptionSiteRepository adoptionSiteRepository) {
        this.adoptionSiteRepository = adoptionSiteRepository;
    }


    /**
     * Returns all adoption sites with an approved ({@code 'A'}) status.
     *
     * @return list of approved {@link AdoptionSite} entities
     */
    public List<AdoptionSite> findAllApproved() {
        return adoptionSiteRepository.findByStatus('A');
    }


    /**
     * Returns all adoption sites regardless of status, mapped to DTOs.
     *
     * @return list of {@link AdoptionSiteDTO} for all sites
     */
    public List<AdoptionSiteDTO> findAll() {
        return adoptionSiteRepository.findAll()
            .stream()
            .map(AdoptionSiteDTO::fromEntity)
            .toList();
    }


    /**
     * Submits a new adoption site for admin review with a default pending status.
     * Throws if the URL is already registered.
     *
     * @param request payload containing the new site's details
     * @return DTO of the newly created site
     * @throws SiteAlreadyExistsException if the URL already exists
     */
    public AdoptionSiteDTO submitNewSite(NewAdoptionSiteRequest request) {
        if (adoptionSiteRepository.existsByUrl(request.url())) {
            throw new SiteAlreadyExistsException(request.url());
        }

        AdoptionSite site = new AdoptionSite();
        site.setUrl(request.url());
        site.setName(request.name());
        site.setEmail(request.email());
        site.setPhone(request.phone());

        AdoptionSite savedSite = adoptionSiteRepository.save(site);

        return AdoptionSiteDTO.fromEntity(savedSite);
    }


    /**
     * Approves a pending site by setting its status to {@code 'A'}.
     *
     * @param id ID of the site to approve
     * @throws ResourceNotFoundException if no site exists with the given ID
     */
    @Transactional
    public void approveNewSiteRequest(int id) {
        AdoptionSite site = adoptionSiteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AdoptionSite", id));

        site.setStatus('A');
    }


   /**
     * Denies a pending site by setting its status to {@code 'D'}.
     *
     * @param id ID of the site to deny
     * @throws ResourceNotFoundException if no site exists with the given ID
     */  
    @Transactional
    public void denyNewSiteRequest(int id) {
        AdoptionSite site = adoptionSiteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AdoptionSite", id));
        
        site.setStatus('D');
    }
}
