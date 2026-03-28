package com.pink.pfa.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.pink.pfa.config.TestDataConfig;
import com.pink.pfa.config.TestcontainersConfiguration;
import com.pink.pfa.models.User;
import com.pink.pfa.repos.AdoptionSiteRepository;
import com.pink.pfa.repos.UserRepository;
import com.pink.pfa.services.AdoptionSiteService;
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
    /* SERVICES */
    @Autowired protected JWTService jwtService;
    @Autowired protected CustomUserDetailsService userDetailsService;
    @Autowired protected UserService userService;
    @Autowired protected UserRepository userRepository;
    @Autowired protected AdoptionSiteService adoptionSiteService;
    @Autowired protected AdoptionSiteRepository adoptionSiteRepository;

    /* WEB TEST CLIENT */
    protected WebTestClient webTestClient;

    @Autowired
    void setWebTestClient(@LocalServerPort int port) {
        this.webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .build();
    }

    /* USER SEEDS */
    public static final Map<String, String> SEEDED_USERS = Map.ofEntries(
        Map.entry("austin@pfa.com",   "foobar01"),
        Map.entry("dylan@pfa.com",    "foobar02"),
        Map.entry("keaton@pfa.com",   "foobar03"),
        Map.entry("jordan@pfa.com",   "foobar04"),
        Map.entry("taylor@pfa.com",   "foobar05"),
        Map.entry("morgan@pfa.com",   "foobar06"),
        Map.entry("casey@pfa.com",    "foobar07"),
        Map.entry("riley@pfa.com",    "foobar08"),
        Map.entry("avery@pfa.com",    "foobar09"),
        Map.entry("parker@pfa.com",   "foobar10"),
        Map.entry("quinn@pfa.com",    "foobar11"),
        Map.entry("sage@pfa.com",     "foobar12"),
        Map.entry("drew@pfa.com",     "foobar13"),
        Map.entry("blake@pfa.com",    "foobar14"),
        Map.entry("jamie@pfa.com",    "foobar15"),
        Map.entry("reese@pfa.com",    "foobar16"),
        Map.entry("skyler@pfa.com",   "foobar17"),
        Map.entry("cameron@pfa.com",  "foobar18"),
        Map.entry("alex@pfa.com",     "foobar19"),
        Map.entry("robin@pfa.com",    "foobar20")
    );

    // Wrapper so tests don't have to carry the user and password separately
    public record SeededUser(User user, String password) {}

    /* COMMONLY USED METHODS */
    protected String loginAndGetToken(String email, String password) {
        return webTestClient.post().uri("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {"email":"%s","password":"%s"}
            """.formatted(email, password))
            .exchange()
            .expectStatus().isOk()
            .returnResult(new ParameterizedTypeReference<Map<String, Object>>() {})
            .getResponseBody()
            .blockFirst()
            .get("token")
            .toString();
    }


    protected int getUserIdByEmail (String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return user.getUserId();
    }

    protected SeededUser getRandUserAndPassByRole(User.Role role) {
        List<User> results = new ArrayList<>(
            userRepository.findAll().stream()
                .filter(user -> user.getRole().equals(role))
                .filter(user -> SEEDED_USERS.containsKey(user.getEmail()))
                .toList()
        );
        if (results.isEmpty()) throw new RuntimeException("No users found with role: " + role);
        Collections.shuffle(results);   
        User user = results.get(0);
        String password = SEEDED_USERS.get(user.getEmail());
        if (password == null) {
            throw new IllegalStateException(
                "No seeded password found for " + user.getEmail() + " — is this a non-seeded user?"
            );
        }
        return new SeededUser(user, password);
    }
}
