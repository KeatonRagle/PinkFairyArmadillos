package com.pink.pfa.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pink.pfa.services.JWTService;
import com.pink.pfa.services.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Custom Spring Security filter responsible for authenticating requests
 * using JSON Web Tokens (JWTs).
 * <p>
 * This filter runs once per request and is inserted into the Spring Security
 * filter chain before {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Extract the {@code Authorization} header from incoming requests.</li>
 *   <li>Parse and validate Bearer tokens using {@link JWTService}.</li>
 *   <li>Load the corresponding user via {@link CustomUserDetailsService}.</li>
 *   <li>If valid, set the authenticated user in the {@link SecurityContextHolder}.</li>
 * </ul>
 *
 * Once the {@link SecurityContextHolder} is populated, Spring Security
 * considers the request authenticated and applies authorization rules
 * (e.g., hasRole('ADMIN'), authenticated(), etc.).
 *
 * This enables stateless authentication — no HTTP session is created or used.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;


    /**
     * Processes each incoming HTTP request to perform JWT-based authentication.
     * <p>
     * Execution steps:
     * <ol>
     *   <li>Read the {@code Authorization} header.</li>
     *   <li>If it starts with {@code Bearer }, extract the token.</li>
     *   <li>Extract the email (subject) from the token.</li>
     *   <li>If no authentication is already present in the {@link SecurityContextHolder},
     *       load the user from the database.</li>
     *   <li>Validate the token (signature + expiration + subject match).</li>
     *   <li>If valid, create a {@link UsernamePasswordAuthenticationToken}
     *       and store it in the SecurityContext.</li>
     * </ol>
     *
     * After this method completes, the request continues through the filter chain.
     * If authentication was successfully set, protected endpoints will allow access.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param filterChain remaining filters in the chain
     * @throws ServletException if filter processing fails
     * @throws IOException if I/O errors occur
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // if the authentication field in the request header is valid, then we grab the email from the token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtService.extractEmail(token);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        // if the email was extracted from the token and there is no security context, continue to set security context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // grab the details of the user 
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // token is valid set security context
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // add to the filter chain 
        filterChain.doFilter(request, response);
    }
}