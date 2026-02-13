package com.pink.pfa.models.details;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.pink.pfa.models.User;


/**
 * Adapter class that bridges the {@link User} entity
 * with Spring Security's authentication framework.
 * <p>
 * This class implements {@link UserDetails}, which is the core contract
 * Spring Security uses to represent an authenticated user.
 *
 * Responsibilities:
 * <ul>
 *   <li>Expose user credentials (email and hashed password).</li>
 *   <li>Provide granted authorities (roles) in the format expected by Spring Security.</li>
 *   <li>Indicate account status (non-expired, non-locked, enabled).</li>
 * </ul>
 *
 * The user's {@link com.pink.pfa.models.User.Role} enum is converted into a
 * {@link GrantedAuthority} using the required "ROLE_" prefix convention.
 *
 * This class is created by {@link com.pink.pfa.services.CustomUserDetailsService}
 * during authentication.
 */
public class UserPrincipal implements UserDetails {

    private User user;


    /**
     * Constructs a UserPrincipal wrapping a {@link User} entity.
     *
     * @param user the persisted user entity
     */
    public UserPrincipal (User user) {
        this.user = user;
    }


    /**
     * Returns the granted authorities for this user.
     * <p>
     * Converts the user's role into a Spring Security authority using the
     * required "ROLE_" prefix convention (e.g., ROLE_USER, ROLE_ADMIN).
     *
     * @return collection containing a single {@link GrantedAuthority}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }


    /**
     * Returns the hashed password stored in the database.
     *
     * @return hashed password string
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }


    /**
     * Returns the username used for authentication.
     * In this application, the email serves as the username.
     *
     * @return user's email
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }


    /** @return true if the account is not expired */
    @Override
    public boolean isAccountNonExpired() { return true; }


    /** @return true if the account is not locked */
    @Override
    public boolean isAccountNonLocked() { return true; }


    /** @return true if the credentials are not expired */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    
    /** @return true if the account is enabled */
    @Override
    public boolean isEnabled() { return true; }
}
