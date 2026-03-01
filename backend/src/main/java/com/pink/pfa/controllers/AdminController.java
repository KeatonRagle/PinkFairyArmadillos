package com.pink.pfa.controllers;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.services.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;


    /**
     * Retrieves all registered users.
     * <p>
     * Access is restricted to ADMIN users via role-based authorization.
     * Returns a structured response containing the user list and a timestamp.
     *
     * @return map containing list of users and request timestamp
     */
    @GetMapping("/getAll")
    public Map<String, Object> getAllUsers() {
        return Map.of(
            "Users: ", userService.findAll(),
            "TimeStamp", Instant.now().toString()
        );
    }
    
    
    /**
     * Promotes a user to ADMIN role.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 204 (No Content) upon successful promotion.
     *
     * @param id ID of the user to promote
     * @return empty {@link ResponseEntity} with 204 status
     */
    @PatchMapping("/{id}/promote")
    public ResponseEntity<Void> promote(@PathVariable int id) {
        userService.promoteToAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
