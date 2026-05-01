package com.pink.pfa.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.UpdateUserEmailRequest;
import com.pink.pfa.controllers.requests.UpdateUserNameRequest;
import com.pink.pfa.controllers.requests.UpdateUserPasswordRequest;
import com.pink.pfa.controllers.requests.UserPrefRequest;
import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.exceptions.ActionNotAllowedException;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.UserAlreadyExistsException;
import com.pink.pfa.exceptions.UserAlreadyRequestedContributor;
import com.pink.pfa.models.UserPreferences;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.models.datatransfer.UserPrefDTO;
import com.pink.pfa.services.UserPrefService;
import com.pink.pfa.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


/**
 * REST controller responsible for handling HTTP requests related to user
 * registration, authentication, retrieval, role management, and account moderation.
 * <p>
 * Exposes endpoints under {@code /api/users} and delegates all business
 * logic to {@link UserService} and {@link UserPrefService}.
 * </p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Register new users and issue JWTs upon successful registration.</li>
 *   <li>Authenticate existing users and issue JWTs upon successful login.</li>
 *   <li>Retrieve user data by ID, email, or from the JWT of the current request.</li>
 *   <li>Manage user preferences (create, retrieve, delete).</li>
 *   <li>Promote and demote users between ROLE_USER, ROLE_CONTRIBUTOR, and ROLE_ADMIN.</li>
 *   <li>Ban and unban user accounts.</li>
 *   <li>Handle contributor role requests and admin approval/denial flows.</li>
 * </ul>
 *
 * <p>Security:</p>
 * <ul>
 *   <li>Role-based access control is enforced using {@link PreAuthorize}.</li>
 *   <li>JWT authentication is handled by the security filter layer.</li>
 *   <li>Locked accounts are rejected at both the filter layer (423) and login endpoint.</li>
 * </ul>
 */
@EnableMethodSecurity
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserPrefService userPrefService;

    public UserController (UserService userService, UserPrefService userPrefService) {
        this.userService = userService;
        this.userPrefService = userPrefService;
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
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<UserDTO> getUserByJWT() {
        try {
            return ResponseEntity.ok().body(userService.findByJWT());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves a specific user's preferences by their unique ID.
     * <p>
     * Returns HTTP 200 (OK) with the corresponding list of {@link UserPreferences} on success,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the unique identifier of the user to retrieve
     * @return {@link ResponseEntity} containing the list of {@link UserPreferences},
     *         an empty 404 if the user does not exist,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/getPrefs")
    public ResponseEntity<List<UserPreferences>> getAllPrefsFromUser(
        @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok().body(userPrefService.findAllByUserId(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Updates the display name of the currently authenticated user.
     * <p>
     * Extracts the user identity from the JWT in the {@code Authorization} header
     * and applies the name change provided in the request body.
     * </p>
     * <p>
     * Returns HTTP 200 (OK) with the updated {@link UserDTO} on success,
     * HTTP 404 (Not Found) if the resolved user no longer exists,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param request       the incoming HTTP request containing the {@code Authorization: Bearer <token>} header
     * @param updateRequest the request body containing the new display name
     * @return {@link ResponseEntity} containing the updated {@link UserDTO},
     *         an empty 404 if the user does not exist,
     *         or an empty 500 on unexpected error
     */
    @PatchMapping("/me/name")
    public ResponseEntity<UserDTO> updateMyName(
        HttpServletRequest request,
        @Valid @RequestBody UpdateUserNameRequest updateRequest
    ) {
        try {
            return ResponseEntity.ok().body(userService.updateNameByJWT(request, updateRequest));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/me/updateEmail") 
    public ResponseEntity<UserDTO> updateMyEmail(@Valid @RequestBody UpdateUserEmailRequest updateRequest) {
        try {
            return ResponseEntity.ok().body(userService.updateEmailByJWT(updateRequest));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/me/updatePassword") 
    public ResponseEntity<Map<String, Object>> updateMyPassword(@Valid @RequestBody UpdateUserPasswordRequest updateRequest) {
        try {
            return ResponseEntity.ok().body(userService.updatePasswordByJWT(updateRequest));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves the currently authenticated user's preferences (if such authentication has occurred).
     * <p>
     * Returns HTTP 200 (OK) with the corresponding list of {@link UserPreferences} on success,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the unique identifier of the user to retrieve
     * @return {@link ResponseEntity} containing the list of {@link UserPreferences},
     *         an empty 404 if the user does not exist,
     *         or an empty 500 on unexpected error
     */

    @GetMapping("/me/prefs")
    public ResponseEntity<List<UserPrefDTO>> getMyPrefs() {
        try {
            UserDTO authUserDTO = userService.findByJWT();
            return ResponseEntity.ok().body(userPrefService.findAllByUserId(authUserDTO.id()).stream().map(item -> UserPrefDTO.fromEntity(item)).collect(Collectors.toCollection(ArrayList::new)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Creates a new preference tied to the currently authenticated user's preferences 
     * (if such authentication has occurred).
     * <p>
     * Returns HTTP 200 (OK) with the corresponding {@link UserPreferences} on success,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the unique identifier of the user to retrieve
     * @return {@link ResponseEntity} containing the {@link UserPreferences},
     *         an empty 404 if the user does not exist,
     *         or an empty 500 on unexpected error
     */
    @PostMapping("/me/addPref")
    public ResponseEntity<UserPrefDTO> addPref(
        @Valid @RequestBody UserPrefRequest prefReq
    ) {
        try {
            return ResponseEntity.ok().body(UserPrefDTO.fromEntity(userPrefService.createNewPref(prefReq)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deletes a preference by id tied to the currently authenticated user's preferences 
     * (if such authentication has occurred).
     * <p>
     * Returns HTTP 200 (OK) with the corresponding {@link UserPreferences} on success,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the unique identifier of the user to retrieve
     * @return {@link ResponseEntity} containing the {@link UserPreferences},
     *         an empty 404 if the user does not exist,
     *         or an empty 500 on unexpected error
     */
    @DeleteMapping("/me/deletePref")
    public ResponseEntity<Void> deletePref(
        @RequestParam Integer prefId
    ) {
        try {
            userPrefService.deleteUserPref(prefId);
            return ResponseEntity.ok().build();
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
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "user", userService.findById(userService.createUser(request).getUserId()),
                "token", userService.verify(request)
            ));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Authenticates a user and issues a signed JWT upon successful login.
     * <p>
     * Delegates credential verification to {@link UserService}. On success,
     * returns HTTP 200 (OK) with the authenticated user and a JWT.
     * </p>
     * <p>
     * Response body contains:
     * </p>
     * <ul>
     *   <li>{@code "user"} — the authenticated user as a {@link UserDTO}</li>
     *   <li>{@code "token"} — a signed JWT string</li>
     * </ul>
     * <p>
     * Returns HTTP 404 (Not Found) if no account exists for the given email,
     * HTTP 423 (Locked) if the account has been banned or locked by an administrator,
     * or HTTP 401 (Unauthorized) if the credentials are invalid.
     * </p>
     *
     * @param request the request body containing login credentials (email and password)
     * @return {@link ResponseEntity} with a {@link Map} of the {@link UserDTO} and JWT,
     *         an empty 404 if the email is not registered,
     *         an empty 423 if the account is locked,
     *         or an empty 401 if authentication fails
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
        } catch (LockedException e) {
            System.out.println("hit locked Exception in controller");
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Retrieves all registered users.
     * <p>
     * Access is restricted to ADMIN users via role-based authorization.
     * Returns HTTP 200 (ok) along with a list of users upon success.
     *
     * @return list containing users
     */
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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

    /**
     * Demotes a user from their current role back to ROLE_USER.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 204 (No Content) upon successful demotion,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the ID of the user to demote
     * @return empty {@link ResponseEntity} with 204 status on success,
     *         an empty 404 if the user does not exist,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/demoteToUser/{id}")
    public ResponseEntity<Void> demoteToUser(@PathVariable int id) {
        try {
            userService.demoteToUser(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Bans a user account, preventing them from authenticating.
     * <p>
     * Access is restricted to existing ADMIN users. A banned user will receive
     * HTTP 423 (Locked) on any subsequent login attempt.
     * Returns HTTP 204 (No Content) upon success,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * HTTP 409 (Conflict) if the action is not permitted (e.g., user is already banned),
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the ID of the user to ban
     * @return empty {@link ResponseEntity} with 204 status on success,
     *         an empty 404 if the user does not exist,
     *         an empty 409 if the action is not allowed,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/banUser/{id}")
    public ResponseEntity<Void> banUser(@PathVariable int id) {
        try {
            userService.banUser(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ActionNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Unbans a previously banned user account, restoring their ability to authenticate.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 204 (No Content) upon success,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * HTTP 409 (Conflict) if the action is not permitted (e.g., user is not currently banned),
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the ID of the user to unban
     * @return empty {@link ResponseEntity} with 204 status on success,
     *         an empty 404 if the user does not exist,
     *         an empty 409 if the action is not allowed,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/unbanUser/{id}")
    public ResponseEntity<Void> unbanUser(@PathVariable int id) {
        try {
            userService.unbanUser(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ActionNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Submits a request from the currently authenticated user to be promoted
     * to the CONTRIBUTOR role.
     * <p>
     * Access is restricted to users with ROLE_USER. The request is queued for
     * admin review. Returns HTTP 204 (No Content) upon successful submission,
     * HTTP 404 (Not Found) if the authenticated user no longer exists,
     * HTTP 409 (Conflict) if the user has already submitted a pending contributor request,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @return empty {@link ResponseEntity} with 204 status on success,
     *         an empty 404 if the user does not exist,
     *         an empty 409 if a request has already been submitted,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/requestContributor")
    public ResponseEntity<Void> requestContributor() {
        try {
            userService.requestContributor();
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserAlreadyRequestedContributor e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Denies a pending contributor role request for the specified user.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 204 (No Content) upon successful denial,
     * HTTP 404 (Not Found) if no user exists with the given ID,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @param id the ID of the user whose contributor request is being denied
     * @return empty {@link ResponseEntity} with 204 status on success,
     *         an empty 404 if the user does not exist,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/denyContributor/{id}")
    public ResponseEntity<Void> denyContributor(@PathVariable int id) {
        try {
            userService.denyContributor(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves all users whose accounts are currently banned.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 200 (OK) with a list of {@link UserDTO} on success,
     * HTTP 404 (Not Found) if no banned users are found,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @return {@link ResponseEntity} containing a list of banned {@link UserDTO} objects,
     *         an empty 404 if none exist,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getBannedUsers")
    public ResponseEntity<List<UserDTO>> getBannedUsers() {
        try {
            return ResponseEntity.ok().body(userService.findAllBanned());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ActionNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves all users whose accounts are not currently banned.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 200 (OK) with a list of {@link UserDTO} on success,
     * HTTP 404 (Not Found) if no active users are found,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @return {@link ResponseEntity} containing a list of active (unbanned) {@link UserDTO} objects,
     *         an empty 404 if none exist,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUnbannedUsers")
    public ResponseEntity<List<UserDTO>> getUnbannedUsers() {
        try {
            return ResponseEntity.ok().body(userService.findAllUnBanned());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ActionNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves all users with a pending contributor role request awaiting admin review.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 200 (OK) with a list of {@link UserDTO} on success,
     * HTTP 404 (Not Found) if no pending requests are found,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @return {@link ResponseEntity} containing a list of {@link UserDTO} objects
     *         with pending contributor requests,
     *         an empty 404 if none exist,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getRequestedContributor")
    public ResponseEntity<List<UserDTO>> getRequestedContributor() {
        try {
            return ResponseEntity.ok().body(userService.findAllRequestedContributor());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ActionNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves all users whose contributor role requests have been denied.
     * <p>
     * Access is restricted to existing ADMIN users.
     * Returns HTTP 200 (OK) with a list of {@link UserDTO} on success,
     * HTTP 404 (Not Found) if no denied requests are found,
     * or HTTP 500 (Internal Server Error) if an unexpected failure occurs.
     * </p>
     *
     * @return {@link ResponseEntity} containing a list of {@link UserDTO} objects
     *         with denied contributor requests,
     *         an empty 404 if none exist,
     *         or an empty 500 on unexpected error
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getDeniedContributor")
    public ResponseEntity<List<UserDTO>> getDeniedContributor() {
        try {
            return ResponseEntity.ok().body(userService.findAllDeniedContributor());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ActionNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
