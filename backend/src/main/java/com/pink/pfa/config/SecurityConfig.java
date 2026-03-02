package com.pink.pfa.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

//import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Central Spring Security configuration for the application.
 * <p>
 * This class defines how requests are secured and what authentication mechanisms
 * are used. It configures:
 * <ul>
 *   <li><b>Password hashing</b> via {@link PasswordEncoder} (BCrypt) for storing and verifying user passwords.</li>
 *   <li><b>CORS policy</b> via {@link CorsConfigurationSource} to control which browser origins may call the API.</li>
 *   <li><b>Authorization rules</b> defining which endpoints are public vs protected, and which require ADMIN.</li>
 *   <li><b>Stateless authentication</b> by disabling HTTP sessions and using JWTs.</li>
 *   <li><b>JWT request filtering</b> by inserting a custom {@code JwtFilter} before the standard
 *       {@link UsernamePasswordAuthenticationFilter} to process {@code Authorization: Bearer <token>} headers.</li>
 * </ul>
 *
 * Request flow (high-level):
 * <ol>
 *   <li>Incoming HTTP request enters the Spring Security filter chain.</li>
 *   <li>{@code JwtFilter} attempts to extract/validate a Bearer token and set authentication in the SecurityContext.</li>
 *   <li>Spring applies authorization rules (permitAll/authenticated/hasRole).</li>
 *   <li>If authorized, the request proceeds to the controller.</li>
 * </ol>
 *
 * Notes:
 * <ul>
 *   <li>CSRF is disabled because the API is designed for stateless auth using headers (JWT), not cookies/sessions.</li>
 *   <li>CORS only affects browser-based clients; non-browser tools (curl, server-to-server) are not restricted by CORS.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    
    /**
     * Provides the {@link PasswordEncoder} used to hash and verify user passwords.
     * <p>
     * BCrypt is a strong one-way hashing algorithm designed for password storage.
     * The strength factor (12) controls computational cost.
     *
     * @return BCrypt-based {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Autowired
    private JwtFilter jwtFilter;


    /**
     * Defines the Cross-Origin Resource Sharing (CORS) policy for browser clients.
     * <p>
     * CORS determines which frontend origins are allowed to call this API from a browser,
     * which HTTP methods are permitted, and which headers may be sent.
     *
     * This configuration is applied to all endpoints ({@code /**}).
     *
     * @return {@link CorsConfigurationSource} containing the configured CORS rules
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // set what cross-origin browsers are allowed to do
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "https://adoptpetsforall.com"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        // uncomment if we want to use cookies/session auth
        // config.setAllowCredentials(true);

        // apply the rules above to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    /**
     * Configures the Spring Security filter chain (the security pipeline applied to every request).
     * <p>
     * This configuration:
     * <ul>
     *   <li>Enables CORS using the {@link CorsConfigurationSource} bean.</li>
     *   <li>Disables CSRF (appropriate for stateless JWT APIs).</li>
     *   <li>Defines public endpoints (login/register/public) and protected endpoints.</li>
     *   <li>Requires ADMIN role for admin routes.</li>
     *   <li>Disables HTTP sessions by setting {@link SessionCreationPolicy#STATELESS}.</li>
     *   <li>Registers {@code JwtFilter} to authenticate requests carrying Bearer tokens.</li>
     * </ul>
     *
     * @param http Spring Security configuration builder
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(customizer -> customizer.disable()) // disable Cross-Site Request Forgery protection since we pass auth as a header in the request
            .authorizeHttpRequests(request -> request
                //.anyRequest().permitAll() // ONLY UNCOMMENT FOR DEBUG
                .requestMatchers("/api/users/login", "/api/users/register", "/api/public/**").permitAll() // any endpoint starting with /api/public is public and does not require auth
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated() // any endpoint that does not start with /api/public is private and does require auth
                )
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // sets the api to NEVER use HTTP sessions EVER
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        /*
         * You can test this with these curl commands
         * -----------------------------------------------
         * `curl -i http://localhost:8080/api/public/ping`
         *    - This will work even though there is no auth provided since it is a public endpoint
         * -----------------------------------------------
         * `curl -i -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d '{"name":"Austin","email":"austin@pfa.com","password":"test"}'`
         *    - This will add a user to the database
         * -----------------------------------------------
         * `curl -i -X POST http://localhost:8080/api/users/login -H "Content-Type: application/json" -d '{"email":"austin@pfa.com","password":"test"}'`
         *    - This will return a json web token that will be passed with all requests to protected endpoints
         * -----------------------------------------------
         * `curl -i http://localhost:8080/api/users/1 -H "Authorization: Bearer <your-token>"`
         *    - This will use the json web token generated for our session to get a user by id
         * -----------------------------------------------
         * `curl -i http://localhost:8080/api/users/1`
         *    - This will fail since it is a protected endpoint and no auth was provided
         * -----------------------------------------------
         * `curl -i http://localhost:8080/api/users/1 -H "Authorization: Bearer nope"`
         *    - This will fail since it is a protected endpoint and the json web token does not match to any user
         * */

        return http.build();
    }


    /**
     * Configures the {@link AuthenticationProvider} used for username/password authentication.
     * <p>
     * Uses {@link DaoAuthenticationProvider}, which loads users via {@link UserDetailsService}
     * (our {@code CustomUserDetailsService}) and verifies passwords using the configured
     * {@link PasswordEncoder}.
     *
     * This is primarily used during login when converting submitted credentials into an
     * authenticated {@link Authentication}.
     *
     * @param userDetailsService service that loads users from the database
     * @param passwordEncoder encoder used to verify hashed passwords
     * @return configured {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


    /**
     * Exposes Spring Security's {@link AuthenticationManager}.
     * <p>
     * The {@link AuthenticationManager} is the component that accepts an unauthenticated
     * {@link Authentication} (like {@link UsernamePasswordAuthenticationToken}) and returns
     * an authenticated one if credentials are valid.
     *
     * This is commonly called from a login service/controller to verify user credentials.
     *
     * @param config Spring's authentication configuration
     * @return {@link AuthenticationManager} for the application
     * @throws Exception if the manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
