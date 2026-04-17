package com.pink.pfa.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.controllers.requests.UserPrefRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.User;
import com.pink.pfa.models.UserPref;
import com.pink.pfa.models.UserPref.Preference;
import com.pink.pfa.repos.UserPrefRepository;

/**
 * Integration test suite for {@link UserPrefService}.
 *
 * <p>Spins up a full Spring context backed by a Testcontainers MySQL instance and
 * seeds it with known users via {@link com.pink.pfa.config.TestDataConfig}.
 * Tests verify correct preference creation, retrieval, deletion, and exception
 * behavior for invalid lookups.
 */
@ExtendWith(MockitoExtension.class)
class UserPrefServiceTest extends PfaBase {

    private final UserPrefService userPrefService;
    private final UserPrefRepository userPrefRepository;

    @Autowired
    public UserPrefServiceTest(
        UserPrefService userPrefService,
        UserPrefRepository userPrefRepository
    ) {
        this.userPrefService = userPrefService;
        this.userPrefRepository = userPrefRepository;
    }

    // -------------------------------------------------------------------------
    // findAllByUserId
    // -------------------------------------------------------------------------

    /**
     * Verifies that findAllByUserId returns an empty list for a user with no prefs,
     * confirming no cross-user data leaks and no exceptions on empty results.
     */
    @Test
    @Transactional
    void findAllByUserId_ShouldReturnEmptyForUserWithNoPrefs() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        int userId = seededUser.user().getUserId();

        List<UserPref> prefs = userPrefService.findAllByUserId(userId);

        assertNotNull(prefs, "Result should not be null");
        assertTrue(prefs.isEmpty(), "Expected no prefs for a freshly seeded user");
    }

    /**
     * Verifies that findAllByUserId returns only prefs belonging to the given user,
     * confirming user scoping is applied correctly.
     */
    @Test
    @Transactional
    void findAllByUserId_ShouldReturnOnlyPrefsForThatUser() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        User user = seededUser.user();
        int userId = user.getUserId();

        mockSecurityContext(user);

        userPrefService.createNewPref(new UserPrefRequest(Preference.BREED, "Labrador"));
        userPrefService.createNewPref(new UserPrefRequest(Preference.GENDER, "female"));

        List<UserPref> prefs = userPrefService.findAllByUserId(userId);

        assertEquals(2, prefs.size(), "Expected exactly 2 prefs for this user");
        assertTrue(prefs.stream().allMatch(p -> p.getUser().getUserId() == userId),
            "All prefs should belong to the authenticated user");
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    /**
     * Verifies that findById returns the correct pref when given a valid ID.
     */
    @Test
    @Transactional
    void findById_WithValidId_ShouldReturnCorrectPref() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        mockSecurityContext(seededUser.user());

        UserPref created = userPrefService.createNewPref(
            new UserPrefRequest(Preference.SIZE, "Medium")
        );

        UserPref found = userPrefService.findById(created.getId());

        assertNotNull(found, "Expected a pref to be returned");
        assertEquals(Preference.SIZE, found.getPreferenceType());
        assertEquals("Medium", found.getValue());
    }

    /**
     * Verifies that findById throws ResourceNotFoundException for a nonexistent ID.
     */
    @Test
    @Transactional
    void findById_WithInvalidId_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
            () -> userPrefService.findById(999999));
    }

    // -------------------------------------------------------------------------
    // createNewPref
    // -------------------------------------------------------------------------

    /**
     * Verifies that createNewPref persists a new preference tied to the authenticated user.
     */
    @Test
    @Transactional
    void createNewPref_ShouldPersistAndReturnPref() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        mockSecurityContext(seededUser.user());

        UserPref result = userPrefService.createNewPref(
            new UserPrefRequest(Preference.BREED, "Poodle")
        );

        assertNotNull(result, "Created pref should not be null");
        assertNotNull(result.getId(), "Saved pref should have a generated ID");
        assertEquals(Preference.BREED, result.getPreferenceType());
        assertEquals("Poodle", result.getValue());
        assertEquals(seededUser.user().getUserId(), result.getUser().getUserId());
    }

    /**
     * Verifies that multiple prefs of different types can be created for the same user.
     */
    @Test
    @Transactional
    void createNewPref_ShouldAllowMultiplePrefsPerUser() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        mockSecurityContext(seededUser.user());

        userPrefService.createNewPref(new UserPrefRequest(Preference.BREED,   "Labrador"));
        userPrefService.createNewPref(new UserPrefRequest(Preference.GENDER,  "female"));
        userPrefService.createNewPref(new UserPrefRequest(Preference.AGE_MIN, "1"));
        userPrefService.createNewPref(new UserPrefRequest(Preference.AGE_MAX, "5"));
        userPrefService.createNewPref(new UserPrefRequest(Preference.SIZE,    "Large"));

        List<UserPref> prefs = userPrefService.findAllByUserId(
            seededUser.user().getUserId()
        );

        assertEquals(5, prefs.size(), "Expected all 5 preference types to be created");
    }

    // -------------------------------------------------------------------------
    // deleteUserPref
    // -------------------------------------------------------------------------

    /**
     * Verifies that deleteUserPref removes the correct pref and it no longer appears
     * in subsequent queries.
     */
    @Test
    @Transactional
    void deleteUserPref_ShouldRemovePrefFromRepository() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        mockSecurityContext(seededUser.user());

        UserPref created = userPrefService.createNewPref(
            new UserPrefRequest(Preference.GENDER, "male")
        );

        userPrefService.deleteUserPref(created.getId());

        assertThrows(Exception.class,
            () -> userPrefService.findById(created.getId()),
            "Pref should no longer exist after deletion");
    }

    /**
     * Verifies that a user cannot delete a pref that belongs to a different user.
     */
    @Test
    @Transactional
    void deleteUserPref_ShouldThrowWhenPrefDoesNotBelongToUser() {
        // User A creates a pref
        SeededUser userA = getRandUserAndPassByRole(User.Role.ROLE_USER);
        mockSecurityContext(userA.user());
        UserPref created = userPrefService.createNewPref(
            new UserPrefRequest(Preference.SIZE, "Small")
        );

        // User B tries to delete it
        SeededUser userB = getRandUserAndPassByRole(User.Role.ROLE_USER);
        mockSecurityContext(userB.user());

        assertThrows(RuntimeException.class,
            () -> userPrefService.deleteUserPref(created.getId()),
            "Should not be able to delete another user's pref");
    }

    // -------------------------------------------------------------------------
    // findAllByEmail
    // -------------------------------------------------------------------------

    /**
     * Verifies that findAllByEmail returns prefs for the correct user.
     */
    @Test
    @Transactional
    void findAllByEmail_ShouldReturnPrefsForMatchingUser() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        mockSecurityContext(seededUser.user());

        userPrefService.createNewPref(new UserPrefRequest(Preference.BREED, "Beagle"));

        List<UserPref> prefs = userPrefService.findAllByEmail(
            seededUser.user().getEmail()
        );

        assertTrue(prefs.size() >= 1, "Expected at least one pref for this user");
    }

    /**
     * Verifies that findAllByEmail throws ResourceNotFoundException for an unknown email.
     */
    @Test
    @Transactional
    void findAllByEmail_ShouldThrowForUnknownEmail() {
        assertThrows(ResourceNotFoundException.class,
            () -> userPrefService.findAllByEmail("ghost@nowhere.com"));
    }
}