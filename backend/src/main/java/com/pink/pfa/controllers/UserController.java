package com.pink.pfa.controllers;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.CreateUserRequest;
import com.pink.pfa.models.User;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


/**
 * REST controller responsible for handling HTTP requests related to user
 * registration, authentication, retrieval, and role management.
 * <p>
 * This controller exposes endpoints under {@code /api/users} and delegates
 * business logic to {@link UserService}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Register new users.</li>
 *   <li>Authenticate users and issue JWTs.</li>
 *   <li>Retrieve user data (restricted by role where applicable).</li>
 * </ul>
 *
 * Security:
 * <ul>
 *   <li>Role-based access control is enforced using {@link PreAuthorize}.</li>
 *   <li>ADMIN role is required for certain endpoints.</li>
 *   <li>JWT authentication is handled by the security filter layer.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    // Singleton object the controller uses to interface with the database
    @Autowired
    private UserService userService;
 

    /**
     * Retrieves a specific user by their unique ID.
     * <p>
     * Returns a structured response containing the user data and timestamp.
     *
     * @param id unique identifier of the user
     * @return map containing the requested user and request timestamp
     */
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(
        @PathVariable Integer id
    ) {
        return Map.of(
            "ID: ", userService.findById(id),
            "TimeStamp", Instant.now().toString()
        );
    }


    /**
     * Retrieves the currently authenticated user's information based on the
     * JWT provided in the request's Authorization header.
     *
     * <p>This endpoint extracts the JWT from the incoming {@link HttpServletRequest},
     * delegates user resolution to {@code userService}, and returns a response map
     * containing the authenticated user's data along with the current timestamp.</p>
     *
     * @param request the HTTP request containing the Authorization header with a Bearer token
     * @return a {@link Map} containing:
     *         <ul>
     *             <li>"User: " – the {@link UserDTO} of the authenticated user</li>
     *             <li>"TimeStamp" – the current timestamp in ISO-8601 string format</li>
     *         </ul>
     */
    @GetMapping("/findMe")
    public Map<String, Object> getUserByJWT(
        HttpServletRequest request
    ) {
        return Map.of(
            "User: ", userService.findByJWT(request),
            "TimeStamp", Instant.now().toString()
        );
    }


    /**
     * Registers a new user account.
     * <p>
     * Accepts a {@link CreateUserRequest}, delegates creation logic to
     * {@link UserService}, and returns the created user as a {@link UserDTO}.
     *
     * Responds with HTTP 201 (Created) upon successful registration.
     *
     * @param request request body containing user registration data
     * @return {@link ResponseEntity} containing created {@link UserDTO}
     */
    @PostMapping("/register")
    // @RequestBody tells Spring to create a new customer object with the request body. 
    public ResponseEntity<UserDTO> CreateUser (
        @Valid @RequestBody CreateUserRequest request
    ) {
        UserDTO createdUser = userService.createUser(request);
        // Acts as a regular Java object with the added flavor of having the entire HTTP response instead of default 200 (OK)
        // Also lets you use the Builder pattern to do what I did here
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }


    /**
     * Authenticates a user and generates a JWT upon successful login.
     * <p>
     * Delegates credential verification to {@link UserService}. If authentication
     * succeeds, a signed JWT is returned for use in subsequent authenticated requests.
     *
     * @param user object containing login credentials (email and password)
     * @return signed JWT string
     */
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.verify(user);
    }


}
