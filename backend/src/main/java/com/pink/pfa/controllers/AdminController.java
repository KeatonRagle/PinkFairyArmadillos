package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.services.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController (UserService userService) {
        this.userService = userService;
    }


    /**
     * Retrieves all registered users.
     * <p>
     * Access is restricted to ADMIN users via role-based authorization.
     * Returns HTTP 20 (ok) along with a list of users upon success.
     *
     * @return list containing users
     */
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserDTO> users = userService.findAll();
            return ResponseEntity.ok().body(users);
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
    @PatchMapping("/{id}/promoteToAdmin")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable int id) {
        try {
            userService.promoteToAdmin(id);
            return ResponseEntity.noContent().build();
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
    @PatchMapping("/{id}/promoteToContributor")
    public ResponseEntity<Void> promoteToContributor(@PathVariable int id) {
        try {
            userService.promoteToContributor(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
