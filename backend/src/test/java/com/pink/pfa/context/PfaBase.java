package com.pink.pfa.context;

import org.aspectj.internal.lang.annotation.ajcDeclareAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.config.TestcontainersConfiguration;
import com.pink.pfa.repos.UserRepository;
import com.pink.pfa.services.CustomUserDetailsService;
import com.pink.pfa.services.JWTService;
import com.pink.pfa.services.UserService;


/**
 * Abstract base class for all Pets for All integration tests.
 *
 * <p>Establishes a shared Spring application context across all subclasses, enabling
 * Spring's test context caching to reuse a single context and container across the
 * entire test suite — avoiding redundant startup overhead.
 *
 * <p>The following infrastructure is configured for all subclasses:
 * <ul>
 *   <li><b>Testcontainers MySQL</b> — a fresh containerized MySQL instance is started
 *       via {@link TestcontainersConfiguration} and wired in as the application's
 *       datasource through {@code @ServiceConnection}.</li>
 *   <li><b>Seed data</b> — known users (Austin, Dylan, Keaton) and pets (Buddy, Luna, Max)
 *       are inserted before tests run via {@link TestDataConfig}.</li>
 *   <li><b>Random port</b> — Tomcat binds to an ephemeral port, allowing
 *       {@code WebTestClient}-based tests (e.g. {@code ApiSecurityTest}) to make real
 *       HTTP requests without port conflicts.</li>
 *   <li><b>WebDriver disabled</b> — suppresses Selenium auto-configuration that is
 *       not needed in the integration test environment.</li>
 *   <li><b>Test profile</b> — activates the {@code test} Spring profile, allowing
 *       profile-specific configuration to override production defaults.</li>
 * </ul>
 *
 * <p>Subclasses inherit a pre-wired {@link JWTService} instance for tests that need
 * to generate or inspect tokens without going through the HTTP layer.
 *
 * <p><b>Context sharing:</b> subclasses must not redeclare {@code @SpringBootTest} or
 * {@code @Import} — doing so produces a different context key and defeats caching,
 * causing additional container startups. Simply extend this class with no extra
 * Spring annotations.
 *
 * <p>Seeded test credentials:
 * <pre>
 *   austin@pfa.com  / foobar1
 *   dylan@pfa.com   / foobar12
 *   keaton@pfa.com  / foobar13
 * </pre>
 */
@Import({TestcontainersConfiguration.class, TestDataConfig.class})
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.test.webdriver.enabled=false"
)
@ActiveProfiles("test")
public abstract class PfaBase {
    @Autowired
    protected JWTService jwtService;

    @Autowired
    protected CustomUserDetailsService userDetailsService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserRepository userRepository;
}
