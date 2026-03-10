package com.pink.pfa.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.config.TestcontainersConfiguration;

@Import({TestcontainersConfiguration.class, TestDataConfig.class})
@SpringBootTest
class JWTServiceTest {

    private final JWTService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public JWTServiceTest (JWTService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    private final String email = "austin@pfa.com";


    @Test
    void shouldGenerateValidToken() {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token, userDetails));
    }
}
