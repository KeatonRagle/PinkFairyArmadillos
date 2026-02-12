package com.pink.pfa.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pink.pfa.models.User;
import com.pink.pfa.models.details.UserPrincipal;
import com.pink.pfa.repos.UserRepository;


/**
 * Spring Security service responsible for loading user-specific authentication
 * data from the database.
 * <p>
 * This class implements {@link UserDetailsService}, which is a core Spring
 * Security interface used during authentication. When a login attempt occurs,
 * Spring Security calls {@link #loadUserByUsername(String)} to retrieve the
 * corresponding user record.
 *
 * Responsibilities:
 * <ul>
 *   <li>Normalize the provided email (trim + lowercase) to ensure consistent lookups.</li>
 *   <li>Query the database via {@link UserRepository}.</li>
 *   <li>Throw {@link UsernameNotFoundException} if the user does not exist.</li>
 *   <li>Wrap the {@link User} entity in a {@link UserPrincipal}, which implements
 *       {@link UserDetails} for Spring Security.</li>
 * </ul>
 *
 * This class acts as the bridge between the application's persistence layer
 * and Spring Security’s authentication mechanism.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    // might use later
    //private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);


    /**
     * Loads a user from the database by email for authentication purposes.
     * <p>
     * The provided email is normalized (trimmed and converted to lowercase)
     * before querying the database to prevent case-sensitivity issues.
     *
     * If no matching user is found, a {@link UsernameNotFoundException}
     * is thrown, which signals Spring Security that authentication should fail.
     *
     * If a user is found, it is wrapped in a {@link UserPrincipal}, which
     * provides Spring Security with the required credentials and authorities.
     *
     * @param email the email used as the username during login
     * @return a {@link UserDetails} implementation representing the authenticated user
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = email.trim().toLowerCase(); // normalize by trimming white space and lowercasing all chars
        User user = repo.findByEmail(normalized); // find user from the database

        // throw exception and debug log if the user is not found
        if (user == null) {
            System.out.println("User not found" + email);
            throw new UsernameNotFoundException("User not found: " + email);
        }

        // wrap the user in a UserPrincipal object for spring security
        return new UserPrincipal(user);  
    }
}
