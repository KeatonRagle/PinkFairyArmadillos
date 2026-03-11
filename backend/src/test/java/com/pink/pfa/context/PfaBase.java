package com.pink.pfa.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.config.TestcontainersConfiguration;
import com.pink.pfa.services.JWTService;

@Import({TestcontainersConfiguration.class, TestDataConfig.class})
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.test.webdriver.enabled=false"
)
@ActiveProfiles("test")
public abstract class PfaBase {
    @Autowired
    protected JWTService jwtService;
}
