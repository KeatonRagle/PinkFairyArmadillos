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
 * 
 * JwtFilter
 * */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * 
     * Custom filter for setting security context useing JWT tokens
     * @param HttpServletRequest
     * @param HttpServletResponse
     * @param FilterChain
     * @throws ServletException
     * @throws IOException
     * */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // if the authentication field in the request header is valid, then we grab the email from the token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            email = jwtService.extractEmail(token);
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
