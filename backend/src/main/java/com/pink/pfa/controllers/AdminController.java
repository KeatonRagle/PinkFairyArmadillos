package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.AdoptionSiteRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.datatransfer.AdoptionSiteDTO;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.services.UserService;
import com.pink.pfa.services.AdoptionSiteService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AdoptionSiteService adoptionSiteService;

    public AdminController (UserService userService, AdoptionSiteService adoptionSiteService) {
        this.userService = userService;
        this.adoptionSiteService = adoptionSiteService;
    }


    /**
     * Retrieves all registered users.
     * <p>
     * Access is restricted to ADMIN users via role-based authorization.
     * Returns HTTP 200 (ok) along with a list of users upon success.
     *
     * @return list containing users
     */
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            return ResponseEntity.ok().body(userService.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
    
    
    /**
     * Promotes a user to ADMIN role.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 204 (No Content) upon successful promotion.
     *
     * Returns HTTP 500 (Internal Server Error) if unsuccessful.
     *
     * @param id ID of the user to promote
     * @return empty {@link ResponseEntity} with 204 status
     */
    @PatchMapping("/promoteToAdmin/{id}")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable int id) {
        try {
            userService.promoteToAdmin(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Promotes a user to CONTRIBUTOR role.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 204 (No Content) upon successful promotion.
     *
     * Returns HTTP 500 (Internal Server Error) if unsuccessful.
     *
     * @param id ID of the user to promote
     * @return empty {@link ResponseEntity} with corresponding status
     */
    @PatchMapping("/promoteToContributor/{id}")
    public ResponseEntity<Void> promoteToContributor(@PathVariable int id) {
        try {
            userService.promoteToContributor(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/getSite/{id}")
    public ResponseEntity<AdoptionSiteDTO> getSiteById(@PathVariable int id) {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findSiteById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getAllSites")
    public ResponseEntity<List<AdoptionSiteDTO>> getAllSites() {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/getApprovedSites")
    public ResponseEntity<List<AdoptionSiteDTO>> getApprovedSites() {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findApproved());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getDeniedSites")
    public ResponseEntity<List<AdoptionSiteDTO>> getDeniedSites() {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findDenied());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getPendingSites")
    public ResponseEntity<List<AdoptionSiteDTO>> getPendingSites() {
        try {
            return ResponseEntity.ok().body(adoptionSiteService.findPending());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PatchMapping("/approveSite/{id}")
    public ResponseEntity<Void> approveSite(@PathVariable int id){
        System.out.println("approve site endpoint hit");
        try {
            adoptionSiteService.approveSite(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PatchMapping("/denySite/{id}")
    public ResponseEntity<Void> denySite(@PathVariable int id){
        System.out.println("deny site endpoint hit");
        try {
            adoptionSiteService.denySite(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


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
