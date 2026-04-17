package com.pink.pfa.controllers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import com.pink.pfa.context.PfaBase;
import com.pink.pfa.models.User;

class UserControllerTest extends PfaBase {
    // -------------------------------------------------------------------------
    // getUserById
    // -------------------------------------------------------------------------

    /**
     * Validates that USERs cannot access this endpoint
     */
    @Test
    void getUserById_WithUserToken_ShouldReturn403() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        int userId = getUserIdByEmail(user.user().getEmail());

        String token = loginAndGetToken(user.user().getEmail(), user.password());
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        int userId = getUserIdByEmail(user.user().getEmail());

        webTestClient.get().uri("/api/users/" + userId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Validates that ADMINs can access this endpoint
     */
    @Test
    void getUserById_WithAdminToken_ShouldReturn200() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        int userId = getUserIdByEmail(user.user().getEmail());
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);

        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
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
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());

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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());
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
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
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
        webTestClient.get().uri("/api/users/findMe")
                .header("Authorization", "Bearer " + "somethingreallyinvalid")
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String name = user.user().getName();
        String email = user.user().getEmail();
        String pass = user.password();

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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String name = user.user().getName();
        String email = user.user().getEmail();
        String pass = user.password();
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

    @Test void login_WithInvalidPassword_ShouldReturn401() { SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String email = user.user().getEmail();
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String email = "the_wrong_email@pfa.com";
        String pass = user.password();
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String name = user.user().getName();
        String email = user.user().getEmail();
        String pass = user.password();
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
                    Map returnedUser = (Map) body.get("user");
                    assertThat(returnedUser).isNotNull();
                    assertThat(returnedUser.get("name")).isEqualTo(name);
                    assertThat(returnedUser.get("email")).isEqualTo(email);
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
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
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
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());
        webTestClient.get().uri("/api/users/getAllUsers")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getAllUsers_WithContributorToken_ShouldReturn403() {
        SeededUser contributor = getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR);
        String token = loginAndGetToken(contributor.user().getEmail(), contributor.password());
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
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/promoteToAdmin/" + 6)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void promoteToAdmin_WithUserToken_ShouldReturn403() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());
        webTestClient.patch().uri("/api/users/promoteToAdmin/" + 2)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void promoteToAdmin_WithContributorToken_shouldReturn403() {
        SeededUser contributor = getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR);
        String token = loginAndGetToken(contributor.user().getEmail(), contributor.password());
        webTestClient.patch().uri("/api/users/promoteToAdmin/" + 3)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void promoteToAdmin_WithInvalidUserId_ShouldReturn404() {
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
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
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/promoteToContributor/" + 6)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void promoteToContributor_WithUserToken_ShouldReturn403() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());
        webTestClient.patch().uri("/api/users/promoteToContributor/" + 2)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void promoteToContributor_WithContributorToken_shouldReturn403() {
        SeededUser contributor = getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR);
        String token = loginAndGetToken(contributor.user().getEmail(), contributor.password());
        webTestClient.patch().uri("/api/users/promoteToContributor/" + 3)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void promoteToContributor_WithInvalidUserId_ShouldReturn404() {
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/promoteToContributor/" + 99999)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // banUser
    // -------------------------------------------------------------------------
    @Test
    void banUser_WithUserToken_ShouldReturn403() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());
        webTestClient.patch().uri("/api/users/banUser/" + user.user().getUserId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }

    @Test
    void banUser_WithContributorToken_ShouldReturn403() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        SeededUser contributor = getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR);
        String token = loginAndGetToken(contributor.user().getEmail(), contributor.password());
        webTestClient.patch().uri("/api/users/banUser/" + user.user().getUserId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }

    @Test
    void banUser_WithAdminToken_AndValidUserId_WhereUserIsUnbanned_ShouldReturn204() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/banUser/" + user.user().getUserId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }

    @Test
    void banUser_WithAdminToken_AndValidUserId_WhereUserIsAlreadyBanned_ShouldReturn409() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        user.user().setIsBanned(true); 
        userRepository.save(user.user());

        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/banUser/" + user.user().getUserId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_CONFLICT);

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }

    @Test
    void banUser_WithAdminToken_AndInvalidUserId_ShouldReturn404() {
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/banUser/" + 99999)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void banUser_WithNoToken_ShouldReturn401() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
    
        webTestClient.patch().uri("/api/users/banUser/" + user.user().getUserId())
                .exchange()
                .expectStatus().isUnauthorized();

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }
    
    // -------------------------------------------------------------------------
    // unbanUser
    // -------------------------------------------------------------------------
    @Test
    void unbanUser_WithUserToken_ShouldReturn403() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());
        webTestClient.patch().uri("/api/users/unbanUser/" + user.user().getUserId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }

    @Test
    void unbanUser_WithContributorToken_ShouldReturn403() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        SeededUser contributor = getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR);
        String token = loginAndGetToken(contributor.user().getEmail(), contributor.password());
        webTestClient.patch().uri("/api/users/unbanUser/" + user.user().getUserId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }

    @Test
    void unbanUser_WithAdminToken_AndValidUserId_WhereUserIsBanned_ShouldReturn204() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        user.user().setIsBanned(true); 
        userRepository.save(user.user());

        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/unbanUser/" + user.user().getUserId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }

    @Test
    void unbanUser_WithAdminToken_AndValidUserId_WhereUserIsNotBanned_ShouldReturn409() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/unbanUser/" + user.user().getUserId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_CONFLICT);

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }

    @Test
    void unbanUser_WithAdminToken_AndInvalidUserId_ShouldReturn404() {
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());
        webTestClient.patch().uri("/api/users/unbanUser/" + 99999)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test 
    void unbanUser_WithNoToken_ShouldReturn401() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        user.user().setIsBanned(true); 
        userRepository.save(user.user());
    
        webTestClient.patch().uri("/api/users/unbanUser/" + user.user().getUserId())
                .exchange()
                .expectStatus().isUnauthorized();

        User updated = userRepository.findById(user.user().getUserId()).orElseThrow();
        updated.setIsBanned(false); 
        userRepository.save(updated);
    }


    // -------------------------------------------------------------------------
    // requestContributor
    // -------------------------------------------------------------------------
    @Test
    void requestContributor_WithUserToken_WithUnreqestedStatus_ShouldReturn204() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.patch().uri("/api/users/requestContributor")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        user.user().setRequestedContributor('N');
		userRepository.save(user.user());
    }

    @Test
    void requestContributor_WithUserToken_WithReqestedStatus_ShouldReturn409() {
        SeededUser user = getRandUserAndPassByRole(User.Role.ROLE_USER);
        user.user().setRequestedContributor('P');
        userRepository.save(user.user());
        String token = loginAndGetToken(user.user().getEmail(), user.password());

        webTestClient.patch().uri("/api/users/requestContributor")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_CONFLICT);

        user.user().setRequestedContributor('N');
        userRepository.save(user.user());
    }

    @Test
    void requestContributor_WithContributorToken_ShouldReturn_403() {
        SeededUser contributor = getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR);
        String token = loginAndGetToken(contributor.user().getEmail(), contributor.password());

        webTestClient.patch().uri("/api/users/requestContributor")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        contributor.user().setRequestedContributor('N');
        userRepository.save(contributor.user());
    }
    
    @Test
    void requestContributor_WithAdminToken_ShouldReturn403() {
        SeededUser admin = getRandUserAndPassByRole(User.Role.ROLE_ADMIN);
        String token = loginAndGetToken(admin.user().getEmail(), admin.password());

        webTestClient.patch().uri("/api/users/requestContributor")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        admin.user().setRequestedContributor('N');
        userRepository.save(admin.user());
    }

    @Test
    void requestcontributor_WithNoToken_ShouldReturn401() {
        webTestClient.patch().uri("/api/users/requestContributor")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
