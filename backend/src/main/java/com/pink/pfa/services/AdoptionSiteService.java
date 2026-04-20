package com.pink.pfa.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.controllers.requests.AdoptionSiteRequest;
import com.pink.pfa.exceptions.NoAdoptionSitesException;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.SiteAlreadyExistsException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.datatransfer.AdoptionSiteDTO;
import com.pink.pfa.repos.AdoptionSiteRepository;
import com.pink.pfa.repos.UserRepository;
import com.pink.pfa.models.details.UserPrincipal;

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

    @Autowired private UserRepository userRepository;

    public AdoptionSiteService (AdoptionSiteRepository adoptionSiteRepository) {
        this.adoptionSiteRepository = adoptionSiteRepository;
    }


    public AdoptionSiteDTO findSiteById(int id) {
        return adoptionSiteRepository.findById(id)
            .map(AdoptionSiteDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("AdoptionSite", id));
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

    public List<AdoptionSiteDTO> findSubmitionsByJwt() {
        return null;
    }


    public List<AdoptionSiteDTO> findApproved() {
        return adoptionSiteRepository.findByStatus('A')
            .stream()
            .map(AdoptionSiteDTO::fromEntity)
            .toList();
    }


    public List<AdoptionSiteDTO> findDenied() {
        return adoptionSiteRepository.findByStatus('D')
            .stream()
            .map(AdoptionSiteDTO::fromEntity)
            .toList();
    }


    public List<AdoptionSiteDTO> findPending() {
        return adoptionSiteRepository.findByStatus('P')
            .stream()
            .map(AdoptionSiteDTO::fromEntity)
            .toList();
    }


    /**
     * Returns all adoption sites with an approved ({@code 'A'}) status.
     *
     * @return list of approved {@link AdoptionSite} entities
     */
    public List<AdoptionSite> findAllForScrape() {
        List<AdoptionSite> sites = adoptionSiteRepository.findByStatus('A');
        if (sites.isEmpty())
            throw new NoAdoptionSitesException();
        return sites;
    }


    /**
     * Submits a new adoption site for admin review with a default pending status.
     * Throws if the URL is already registered.
     *
     * @param request payload containing the new site's details
     * @return DTO of the newly created site
     * @throws SiteAlreadyExistsException if the URL already exists
     */
    public AdoptionSiteDTO submitNewSite(AdoptionSiteRequest request) {
        if (adoptionSiteRepository.existsByUrl(request.url())) {
            throw new SiteAlreadyExistsException(request.url());
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new ResourceNotFoundException("User", "Token either unreadable or not provided");
        String email = ((UserPrincipal) auth.getPrincipal()).getUsername();

        AdoptionSite site = new AdoptionSite();
        site.setUrl(request.url());
        site.setName(request.name());
        site.setEmail(request.email());
        site.setPhone(request.phone());
        site.setSubmittedAt(LocalDate.now());
        site.setUser(userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email)) 
        );

        AdoptionSite savedSite = adoptionSiteRepository.save(site);

        return AdoptionSiteDTO.fromEntity(savedSite);
    }


    @Transactional
    public AdoptionSiteDTO editSite(AdoptionSiteRequest request, int id) {
        AdoptionSite site = adoptionSiteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AdoptionSite", id));

        site.setUrl(request.url());
        site.setName(request.name());
        site.setEmail(request.email());
        site.setPhone(request.phone());

        return AdoptionSiteDTO.fromEntity(site);
    }


    /**
     * Approves a pending site by setting its status to {@code 'A'}.
     *
     * @param id ID of the site to approve
     * @throws ResourceNotFoundException if no site exists with the given ID
     */
    @Transactional
    public void approveSite(int id) {
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
    public void denySite(int id) {
        AdoptionSite site = adoptionSiteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AdoptionSite", id));
        
        site.setStatus('D');
    }
}
