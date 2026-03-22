package com.pink.pfa.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.UserAlreadyExistsException;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


/**
 * REST controller responsible for handling HTTP requests related to user
 * registration, authentication, and retrieval.
 * <p>
 * Exposes endpoints under {@code /api/users} and delegates all business
 * logic to {@link UserService}.
 * </p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Register new users and issue JWTs upon successful registration.</li>
 *   <li>Authenticate existing users and issue JWTs upon successful login.</li>
 *   <li>Retrieve user data by ID or from the JWT of the current request.</li>
 * </ul>
 *
 * <p>Security:</p>
 * <ul>
 *   <li>Role-based access control is enforced using {@link PreAuthorize}.</li>
 *   <li>JWT authentication is handled by the security filter layer.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController (UserService userService) {
        this.userService = userService;
    }
 

    /**
     * Retrieves a specific user by their unique ID.
     * <p>
     * Returns HTTP 200 (OK) with the corresponding {@link UserDTO} on success,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the unique identifier of the user to retrieve
     * @return {@link ResponseEntity} containing the {@link UserDTO},
     *         an empty 404 if the user does not exist,
     *         or an empty 500 on unexpected error
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
        @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok().body(userService.findById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Retrieves the currently authenticated user based on the JWT in the request.
     * <p>
     * Extracts the Bearer token from the {@code Authorization} header of the
     * incoming {@link HttpServletRequest} and resolves the associated user via
     * {@link UserService}.
     * </p>
     * <p>
     * Returns HTTP 200 (OK) with the authenticated user's {@link UserDTO} on success,
     * HTTP 404 (Not Found) if the token resolves to a user that no longer exists,
     * or HTTP 500 (Internal Server Error) if the token is missing, invalid, or an
     * unexpected failure occurs.
     * </p>
     *
     * @param request the incoming HTTP request containing the {@code Authorization: Bearer <token>} header
     * @return {@link ResponseEntity} containing the authenticated user's {@link UserDTO},
     *         an empty 404 if the resolved user does not exist,
     *         or an empty 500 on unexpected error
     */
    @GetMapping("/findMe")
    public ResponseEntity<UserDTO> getUserByJWT(
        HttpServletRequest request
    ) {
        try {
            return ResponseEntity.ok().body(userService.findByJWT(request));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * Registers a new user account and returns an authentication token.
     * <p>
     * Accepts a validated {@link UserRequest} body, delegates creation to
     * {@link UserService}, and immediately issues a signed JWT for the new user.
     * </p>
     * <p>
     * Returns HTTP 201 (Created) with a response body containing:
     * </p>
     * <ul>
     *   <li>{@code "user"} — the newly created user as a {@link UserDTO}</li>
     *   <li>{@code "token"} — a signed JWT string for immediate authentication</li>
     * </ul>
     * <p>
     * Returns HTTP 409 (Conflict) if the provided email is already associated
     * with an existing account, or HTTP 500 (Internal Server Error) if an
     * unexpected failure occurs.
     * </p>
     *
     * @param request the request body containing registration data (email, password, etc.)
     * @return {@link ResponseEntity} with a {@link Map} of the created {@link UserDTO} and JWT,
     *         an empty 409 if the email is already registered,
     *         or an empty 500 on unexpected error
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest request) {
        User createdUser = userService.createUser(request);

        // Generate token from createdUser identity + roles
        String jwt = userService.verify(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "user", userService.findById(createdUser.getUserId()),
            "token", jwt
        ));
    }


    /**
     * Authenticates a user and issues a signed JWT upon successful login.
     * <p>
     * Delegates credential verification to {@link UserService}. Returns
     * HTTP 200 (OK) with the authenticated user and a JWT on success,
     * HTTP 404 (Not Found) if no account exists for the given email,
     * or HTTP 403 (Forbidden) if the credentials are invalid.
     * </p>
     * <p>
     * Response body contains:
     * </p>
     * <ul>
     *   <li>{@code "user"} — the authenticated user as a {@link UserDTO}</li>
     *   <li>{@code "token"} — a signed JWT string</li>
     * </ul>
     *
     * @param request the request body containing login credentials (email and password)
     * @return {@link ResponseEntity} with a {@link Map} of the {@link UserDTO} and JWT,
     *         an empty 404 if the email is not registered,
     *         or an empty 403 if authentication fails
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserRequest request) {
        try {
            return ResponseEntity.ok().body(Map.of(
                "user", userService.findByEmail(request.email()), 
                "token", userService.verify(request)
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
