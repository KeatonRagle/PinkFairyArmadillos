package com.pink.pfa;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;

import com.pink.pfa.services.CustomUserDetailsService;
import com.pink.pfa.services.JWTService;

@Import({TestcontainersConfiguration.class, TestDataConfig.class})
@SpringBootTest
class JWTServiceTest {

    @Autowired
    private JWTService jwtService;

    @Autowired 
    private CustomUserDetailsService userDetailsService;


    private final String email = "austin@pfa.com";


    @Test
    void shouldGenerateValidToken() {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token, userDetails));
    }
}
