package com.pink.pfa.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import com.pink.pfa.context.PfaBase;
import com.pink.pfa.models.User;

class UserControllerTest extends PfaBase {

    private int getUserIdByEmail (String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return user.getUserId();
    }

    private void promoteUserToAdmin(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        userService.promoteToAdmin(user.getUserId());
    }

    private void promoteUserToContributor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        userService.promoteToContributor(user.getUserId());
    }

    private void setRoleToUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(User.Role.ROLE_USER);
        userRepository.save(user);
    }


    // -------------------------------------------------------------------------
    // getUserById
    // -------------------------------------------------------------------------

    /**
     * Validates that USERs cannot access this endpoint
     */
    @Test
    void getUserById_WithUserToken_ShouldReturn403() {
        setRoleToUser("morgan@pfa.com");
        int userId = getUserIdByEmail("morgan@pfa.com");

        String token = loginAndGetToken("morgan@pfa.com", "foobar16");
        webTestClient.get().uri("/api/users/" + userId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    /**
     * Validates that this endpoint requires authentication
     */
    @Test
    void getUserId_WithNoToken_ShouldReturn401() {
        int userId = getUserIdByEmail("morgan@pfa.com");
        webTestClient.get().uri("/api/users/" + userId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Validates that ADMINs can access this endpoint
     */
    @Test
    void getUserById_WithAdminToken_ShouldReturn200() {
        int userId = getUserIdByEmail("morgan@pfa.com");
        promoteUserToAdmin("austin@pfa.com");

        String token = loginAndGetToken("austin@pfa.com", "foobar1");
        webTestClient.get().uri("/api/users/" + userId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Validates that a 404 (not found) code is returned when a user doesnt exist
     */
    @Test
    void getUserById_WithInvalidId_ShouldReturn404() {
        promoteUserToAdmin("austin@pfa.com");

        String token = loginAndGetToken("austin@pfa.com", "foobar1");
        webTestClient.get().uri("/api/users/" + 99999)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }


    // -------------------------------------------------------------------------
    // findMe
    // -------------------------------------------------------------------------

    /**
     * Validates that USERs can access this endpoint
     */
    @Test
    void findMe_WithValidUserToken_ShouldReturn200() {
        String token = loginAndGetToken("morgan@pfa.com", "foobar16");
        webTestClient.get().uri("/api/users/findMe")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Validates that ADMINs can access this endpoint
     */
    @Test
    void findMe_WithValidAdminToken_ShouldReturn200() {
        promoteUserToAdmin("austin@pfa.com");
        String token = loginAndGetToken("austin@pfa.com", "foobar1");
        webTestClient.get().uri("/api/users/findMe")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Validates that this endpoint requires authentication
     */
    @Test
    void findMe_WithNoToken_ShouldReturn401() {
        webTestClient.get().uri("/api/users/findMe")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Validates that a valid token is required to access this endpoint
     */
    @Test
    void findMe_WithInvalidToken_ShouldReturn401() {
        promoteUserToAdmin("austin@pfa.com");

        String token = loginAndGetToken("austin@pfa.com", "foobar1") + "thisshouldntbehere";
        webTestClient.get().uri("/api/users/findMe")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }


    // -------------------------------------------------------------------------
    // register
    // -------------------------------------------------------------------------

    /**
     * Validates that USERs cannot access this endpoint
     */
    @Test
    void register_AsNewUser_ShouldReturn201() {
        String name = "Rand User";
        String email = "somethingreallyrandom@email.com";
        String pass = "password";
        webTestClient.post().uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "name": "%s",
                        "email": "%s",
                        "password": "%s"
                    }    
                """.formatted(name, email, pass))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.user.name").isEqualTo(name)
                .jsonPath("$.user.email").isEqualTo(email);
    }

    /**
     * Validates that ADMINs can access this endpoint
     */
    @Test
    void register_AsExsistingUser_ShouldReturn409() {
        String name = "Austin";
        String email = "austin@pfa.com";
        String pass = "foobar12";
        webTestClient.post().uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "name": "%s",
                        "email": "%s",
                        "password": "%s"
                    }    
                """.formatted(name, email, pass))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_CONFLICT);
    }

    @Test
    void register_ShouldReturnUserDTOAndToken() {
        String name = "Rand User";
        String email = "somethingreallyrandom2@email.com";
        String pass = "password";
        webTestClient.post().uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "name": "%s",
                        "email": "%s",
                        "password": "%s"
                    }    
                """.formatted(name, email, pass))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .value(body -> {
                    Map user = (Map) body.get("user");
                    assertThat(user).isNotNull();
                    assertThat(user.get("name")).isEqualTo(name);
                    assertThat(user.get("email")).isEqualTo(email);
                    assertThat(body.get("token")).isNotNull();
                    assertThat(body.get("token").toString()).isNotEmpty();
                });
    }

    
    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------
    
    @Test
    void login_WithValidCredentials_ShouldReturn200() {
        String name = "Austin";
        String email = "austin@pfa.com";
        String pass = "foobar1";
        webTestClient.post().uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }    
                """.formatted(email, pass))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.name").isEqualTo(name)
                .jsonPath("$.user.email").isEqualTo(email);
    }

    @Test
    void login_WithInvalidPassword_ShouldReturn401() {
        String email = "jordan@pfa.com";
        String pass = "the_wrong_password";
        webTestClient.post().uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }    
                """.formatted(email, pass))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_WithInvalidEmail_ShouldReturn404() {
        String email = "the_wrong_email@pfa.com";
        String pass = "foobar14";
        webTestClient.post().uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }    
                """.formatted(email, pass))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void login_ShouldReturnUserDTOAndToken() {
        String name = "Casey";
        String email = "casey@pfa.com";
        String pass = "foobar17";
        webTestClient.post().uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }    
                """.formatted(email, pass))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(body -> {
                    Map user = (Map) body.get("user");
                    assertThat(user).isNotNull();
                    assertThat(user.get("name")).isEqualTo(name);
                    assertThat(user.get("email")).isEqualTo(email);
                    assertThat(body.get("token")).isNotNull();
                    assertThat(body.get("token").toString()).isNotEmpty();
                });
    }


    // -------------------------------------------------------------------------
    // getAllUsers
    // -------------------------------------------------------------------------

    /**
     * Authorized Admin Access
     */
    @Test
    void getAllUsers_WithAdminToken_ShouldReturn200() {
        // promote to admin (just in case) and get token
        promoteUserToAdmin("keaton@pfa.com");
        String token = loginAndGetToken("keaton@pfa.com", "foobar13");
        webTestClient.get().uri("/api/users/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Mass Data Access Attempt
     */
    @Test
    void getAllUsers_WithUserToken_ShouldReturn403() {
        String email = "dylan@pfa.com";
        setRoleToUser(email);
        String token = loginAndGetToken(email, "foobar12");
        webTestClient.get().uri("/api/users/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getAllUsers_WithContributorToken_ShouldReturn403() {
        promoteUserToContributor("taylor@pfa.com"); 
        String token = loginAndGetToken("taylor@pfa.com", "foobar15");
        webTestClient.get().uri("/api/users/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    // -------------------------------------------------------------------------
    // promoteToAdmin
    // -------------------------------------------------------------------------
    
    @Test
    void promoteToAdmin_WithAdminToken_ShouldReturn204() {
        promoteUserToAdmin("keaton@pfa.com");
        String token = loginAndGetToken("keaton@pfa.com", "foobar13");
        webTestClient.patch().uri("/api/users/promoteToAdmin/" + 6)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void promoteToAdmin_WithUserToken_ShouldReturn403() {
        String email = "dylan@pfa.com";
        setRoleToUser(email);
        String token = loginAndGetToken(email, "foobar12");
        webTestClient.patch().uri("/api/users/promoteToAdmin/" + 2)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void promoteToAdmin_WithContributorToken_shouldReturn403() {
        promoteUserToContributor("taylor@pfa.com"); 
        String token = loginAndGetToken("taylor@pfa.com", "foobar15");
        webTestClient.patch().uri("/api/users/promoteToAdmin/" + 3)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void promoteToAdmin_WithInvalidUserId_ShouldReturn404() {
        promoteUserToAdmin("keaton@pfa.com");
        String token = loginAndGetToken("keaton@pfa.com", "foobar13");
        webTestClient.patch().uri("/api/users/promoteToAdmin/" + 99999)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }


    // -------------------------------------------------------------------------
    // promoteToContributor
    // -------------------------------------------------------------------------
    
    @Test
    void promoteToContributor_WithAdminToken_ShouldReturn204() {
        promoteUserToAdmin("keaton@pfa.com");
        String token = loginAndGetToken("keaton@pfa.com", "foobar13");
        webTestClient.patch().uri("/api/users/promoteToContributor/" + 6)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void promoteToContributor_WithUserToken_ShouldReturn403() {
        String email = "dylan@pfa.com";
        setRoleToUser(email);
        String token = loginAndGetToken(email, "foobar12");
        webTestClient.patch().uri("/api/users/promoteToContributor/" + 2)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void promoteToContributor_WithContributorToken_shouldReturn403() {
        promoteUserToContributor("taylor@pfa.com"); 
        String token = loginAndGetToken("taylor@pfa.com", "foobar15");
        webTestClient.patch().uri("/api/users/promoteToContributor/" + 3)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void promoteToContributor_WithInvalidUserId_ShouldReturn404() {
        promoteUserToAdmin("keaton@pfa.com");
        String token = loginAndGetToken("keaton@pfa.com", "foobar13");
        webTestClient.patch().uri("/api/users/promoteToContributor/" + 99999)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

}
