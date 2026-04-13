package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.AdoptionSiteRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.SiteAlreadyExistsException;
import com.pink.pfa.models.datatransfer.AdoptionSiteDTO;
import com.pink.pfa.services.AdoptionSiteService;


@EnableMethodSecurity
@RestController
@RequestMapping("/api/adoptionSite")
public class AdoptionSiteController {

    private final AdoptionSiteService adoptionSiteService;
    
    public AdoptionSiteController (AdoptionSiteService adoptionSiteService) {
        this.adoptionSiteService = adoptionSiteService;
    }


    /**
     * Submits a new adoption site for admin review.
     *
     * @param request payload containing the new site's details
     * @return {@code 200} with the created site; {@code 409} if the URL already exists;
     *         {@code 500} on unexpected error
     */   
    @PreAuthorize("hasAnyRole('CONTRIBUTOR', 'ADMIN')")
    @PostMapping("/submitSite")
    public ResponseEntity<AdoptionSiteDTO> submitNewSite(@RequestBody AdoptionSiteRequest request) {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.submitNewSite(request));
        } catch (SiteAlreadyExistsException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAnyRole('CONTRIBUTOR', 'ADMIN')")
    @GetMapping("/getAllSites")
    public ResponseEntity<List<AdoptionSiteDTO>> getAllSites() {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PreAuthorize("hasAnyRole('CONTRIBUTOR', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AdoptionSiteDTO> getSiteById(@PathVariable int id) {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findSiteById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getApprovedSites")
    public ResponseEntity<List<AdoptionSiteDTO>> getApprovedSites() {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findApproved());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getDeniedSites")
    public ResponseEntity<List<AdoptionSiteDTO>> getDeniedSites() {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findDenied());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getPendingSites")
    public ResponseEntity<List<AdoptionSiteDTO>> getPendingSites() {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findPending());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/approveSite/{id}")
    public ResponseEntity<Void> approveSite(@PathVariable int id){
        try {
            adoptionSiteService.approveSite(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/denySite/{id}")
    public ResponseEntity<Void> denySite(@PathVariable int id){
        try {
            adoptionSiteService.denySite(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/editSite/{id}")
    public ResponseEntity<AdoptionSiteDTO> editSite(@RequestBody AdoptionSiteRequest request, @PathVariable int id) {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.editSite(request, id)); 
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
