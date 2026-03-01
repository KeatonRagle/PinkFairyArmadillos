package com.pink.pfa.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.controllers.requests.CreateUserRequest;
import com.pink.pfa.models.User;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.repos.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;


/**
 * UserService<br>
 * <br>
 * Central service layer for user-related business logic and security workflows.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Read users from the database via {@link UserRepository}.</li>
 *   <li>Convert {@link User} entities to {@link UserDTO} objects to prevent exposing sensitive fields
 *       (e.g., passwords) to controllers / clients.</li>
 *   <li>Create new users by normalizing email input and securely hashing passwords using
 *       {@link PasswordEncoder} before persisting.</li>
 *   <li>Authenticate login attempts using Spring Security's {@link AuthenticationManager} and, on success,
 *       issue a JWT via {@link JWTService} for stateless authentication.</li>
 *   <li>Perform role updates such as promoting a user to {@code ADMIN} inside a transactional boundary.</li>
 * </ul>
 *
 * Notes:
 * <ul>
 *   <li>This class is annotated with {@link Service}, meaning it is a Spring-managed singleton component.</li>
 *   <li>DTO mapping is used so API responses can safely expose only the fields the client should see.</li>
 *   <li>JWT generation is triggered only after Spring Security confirms the provided credentials are valid.</li>
 * </ul>
 */
@Service
@RequestMapping("/api/users")
public class UserService {
    // The singleton backend repository that takes our input and turns it into CRUD (abstracting out our data access layer)
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;


    /**
     * Returns all users as a list of {@link UserDTO}s by fetching entities from the database and mapping
     * each {@link User} to a DTO to avoid exposing sensitive fields.
     *
     * @return list of {@link UserDTO}
     */
    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserDTO::fromEntity)
                .toList();
    }


    /**
     * Fetches a single user by ID and returns it as a {@link UserDTO}.
     * Throws an exception if the user does not exist.
     *
     * @param id database ID of the user
     * @return {@link UserDTO} for the requested user
     */    
    public UserDTO findById(Integer id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new InvalidConfigurationPropertyValueException("Failed to Find ID", null, "User not found"));
    }


    /**
     * Retrieves a {@link UserDTO} based on the JWT provided in the
     * {@code Authorization} header of the given {@link HttpServletRequest}.
     *
     * <p>This method extracts the JWT from the request header, parses the
     * user's email using {@code jwtService}, and looks up the corresponding
     * user in the repository. If a matching user is found, it is converted
     * to a {@link UserDTO}. If no user exists with the extracted email,
     * a {@link UsernameNotFoundException} is thrown.</p>
     *
     * @param request the incoming HTTP request containing the Authorization header
     * @return the {@link UserDTO} corresponding to the authenticated user
     * @throws UsernameNotFoundException if no user is found for the extracted email
     */
    public UserDTO findByJWT(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String email = jwtService.extractEmailFromHeader(authHeader);
        return userRepository.findByEmail(email)
                .map(UserDTO::fromEntity)

                // if this exception gets thrown then 3 things could have happened 
                // 1. the packet degraded during transit
                // 2. the jwt sent is expired
                // 3. (more concerning) someone is generating their own JWTs and sending them to us
                .orElseThrow(() -> new UsernameNotFoundException("Failed to find user by JWT"));
    }


    /**
     * Creates a new user account from a {@link CreateUserRequest}.<br>
     * The email is normalized (trimmed + lowercased) and the password is hashed using {@link PasswordEncoder}
     * before saving the new {@link User} entity to the database.
     *
     * @param request incoming user creation payload
     * @return {@link User} for the newly created user
     */
    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(
            passwordEncoder.encode(request.password())
        );
        user.setRole(User.Role.ROLE_USER);

        User savedUser = userRepository.save(user);
        return savedUser;
    }


    /**
     * Authenticates a login attempt using the provided email and password.
     * If authentication succeeds, generates and returns a JWT for the user's email via {@link JWTService}.
     *
     * @param user object containing login credentials (email + raw password)
     * @return JWT string if authentication succeeds; otherwise a failure message
     */
    public String verify(User user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        if(authentication.isAuthenticated()) return jwtService.generateToken(user.getEmail());
        return "Nope";
    }


    /**
     * Promotes the specified user to the {@code ADMIN} role.
     * Runs within a transaction so the role change is persisted automatically when the method completes.
     *
     * @param userId database ID of the user to promote
     */
    @Transactional
    public void promoteToAdmin(int userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(User.Role.ROLE_ADMIN);
    }
}
