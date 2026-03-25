package com.pink.pfa.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.AdoptionSiteRequest;
import com.pink.pfa.exceptions.SiteAlreadyExistsException;
import com.pink.pfa.models.datatransfer.AdoptionSiteDTO;
import com.pink.pfa.services.AdoptionSiteService;


/**
 * REST controller handling contributor-facing endpoints.
 * <p>
 * Contributors are authenticated users with permission to submit new adoption
 * sites for admin review. All routes are prefixed with {@code /api/contributor}
 * and require {@code ROLE_CONTRIBUTOR} or higher.
 */
@RestController
@RequestMapping("/api/contributor")
public class ContributorController {

    private final AdoptionSiteService adoptionSiteService;

    public ContributorController (AdoptionSiteService adoptionSiteService) {
        this.adoptionSiteService = adoptionSiteService;
    }

    /**
     * Submits a new adoption site for admin review.
     *
     * @param request payload containing the new site's details
     * @return {@code 200} with the created site; {@code 409} if the URL already exists;
     *         {@code 500} on unexpected error
     */   
    @PostMapping("/submitSite")
    public ResponseEntity<AdoptionSiteDTO> submitNewSite(@RequestBody AdoptionSiteRequest request) {
        System.out.println("submit site endpoint hit");
        try {
            return ResponseEntity.ok().body(adoptionSiteService.submitNewSite(request));
        } catch (SiteAlreadyExistsException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
}
