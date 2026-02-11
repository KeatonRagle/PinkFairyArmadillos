package com.pink.pfa.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; 
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



/**
 * SecurityConfig
 * The configeration for spring web security
 * */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * @return the passwordEncoder
     * */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Cross-Origin Resource Sharing configeration. Defines the rules the 
     * client browser needs to follow in order to access our website.  
     * 
     * @return the cross-origin browser ruleset
     * */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // set what cross-origin browsers are allowed to do
        config.setAllowedOrigins(List.of("https://www.adoptpetsforall.com"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        // uncomment if we want to use cookies/session auth
        // config.setAllowCredentials(true);

        // apply the rules above to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    /**
     * Defines the pipeline of filters that every HTTP request passes through
     * before it reaches its controller. This is the authentication and 
     * authorization configuration.
     *
     * @param HttpSecurity http
     * @return the SecurityFilterChain 
     * @throws Exception 
     * */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(customizer -> customizer.disable()) // disable Cross-Site Request Forgery protection since we pass auth as a header in the request
            .authorizeHttpRequests(request -> request
                //.anyRequest().permitAll() // ONLY UNCOMMENT FOR DEBUG
                .requestMatchers(HttpMethod.POST, "/api/users/**").permitAll() // any post request to the user service is permitted without auth
                .requestMatchers("/api/public/**").permitAll() // any endpoint starting with /api/public is public and does not require auth
                .anyRequest().authenticated() // any endpoint that does not start with /api/public is private and does require auth
                )
            .httpBasic(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // sets the api to NEVER use HTTP sessions EVER

        /*
         * You can test this with these curl commands
         * -----------------------------------------------
         * `curl -i http://localhost:8080/api/public/ping`
         *    - This will work even though there is no auth provided since it is a public endpoint
         * -----------------------------------------------
         * `curl -i -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name":"Austin","email":"austin@pfa.com","password":"test"}'`
         *    - This will add a user to the database
         * -----------------------------------------------
         * `curl -i -u austin@pfa.com:test http://localhost:8080/api/users`
         *    - This will use the email and password that we have in the db to get all users
         * -----------------------------------------------
         * `curl -i -u austin@pfa.com:test http://localhost:8080/api/users/1`
         *    - This will use the email and password we have in the db to get a user by id
         * -----------------------------------------------
         * `curl -i http://localhost:8080/api/users`
         *    - This will fail since it is a protected endpoint and no auth was provided
         * -----------------------------------------------
         * 
         * */

        return http.build();
    }

    /**
     * 
     * @param userDetailsService
     * @param passwordEncoder
     * @return 
     * */
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


}
