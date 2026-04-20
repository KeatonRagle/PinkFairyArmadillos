package com.pink.pfa.services;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.pink.pfa.context.PfaBase;
import com.pink.pfa.controllers.requests.ReviewRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Reviews;
import com.pink.pfa.models.User;
import com.pink.pfa.repos.ReviewsRepository;

/**
 * Integration test suite for {@link ReviewService}.
 *
 * <p>Spins up a full Spring context backed by a Testcontainers MySQL instance and
 * seeds it with known users via {@link com.pink.pfa.config.TestDataConfig}.
 * Tests verify correct review creation, retrieval, deletion, and exception
 * behavior for invalid lookups.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest extends PfaBase {

    @Autowired private ReviewService reviewService;
    @Autowired private ReviewsRepository reviewsRepository;

    private AdoptionSite testSite;

    @BeforeEach
    void seedSite() {
        testSite = new AdoptionSite();
        testSite.setName("Controller Test Shelter");
        testSite.setUrl("https://controller-test-" + System.nanoTime() + ".org");
        testSite.setEmail("test@controllershelter.org");
        testSite.setPhone("555-7777");
        testSite.setStatus('P');
        testSite.setSubmittedAt(LocalDate.now());
        testSite.setUser(getRandUserAndPassByRole(User.Role.ROLE_CONTRIBUTOR).user());
        testSite = adoptionSiteRepository.save(testSite);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Creates a review for a random seeded user with the given rating and comment. */
    private Reviews createReviewForRandUser(double rating, String comment) {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        return reviewService.submitNewReview(
            new ReviewRequest(seededUser.user().getUserId(), testSite.getSiteId(), rating, comment)
        );
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    /**
     * Verifies that findAll returns a non-null list, even if no reviews exist yet.
     */
    @Test
    @Transactional
    void findAll_ShouldReturnNonNullList() {
        List<Reviews> reviews = reviewService.findAll(null);
        assertNotNull(reviews, "findAll should never return null");
    }

    /**
     * Verifies that findAll reflects newly submitted reviews.
     */
    @Test
    @Transactional
    void findAll_ShouldIncludeNewlySubmittedReview() {
        int before = reviewService.findAll(null).size();

        createReviewForRandUser(4.5, "Great shelter!");

        int after = reviewService.findAll(null).size();
        assertEquals(before + 1, after, "findAll should grow by one after a submission");
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    /**
     * Verifies that findById returns the correct review when given a valid ID.
     */
    @Test
    @Transactional
    void findById_WithValidId_ShouldReturnCorrectReview() {
        Reviews created = createReviewForRandUser(3.5, "Decent experience.");

        Reviews found = reviewService.findById(created.getReviewId());

        assertNotNull(found, "Expected a review to be returned");
        assertEquals(3.5, found.getRating(), "Rating should match");
        assertEquals("Decent experience.", found.getRwComment(), "Comment should match");
    }

    /**
     * Verifies that findById throws ResourceNotFoundException for a nonexistent ID.
     */
    @Test
    @Transactional
    void findById_WithInvalidId_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
            () -> reviewService.findById(999999),
            "Expected ResourceNotFoundException for unknown ID");
    }

    // -------------------------------------------------------------------------
    // existsById
    // -------------------------------------------------------------------------

    /**
     * Verifies that existsById returns true for a review that exists.
     */
    @Test
    @Transactional
    void existsById_ShouldReturnTrueForExistingReview() {
        Reviews created = createReviewForRandUser(5.0, "Amazing!");

        assertTrue(reviewService.existsById(created.getReviewId()),
            "existsById should return true for a saved review");
    }

    /**
     * Verifies that existsById returns false for a nonexistent ID.
     */
    @Test
    @Transactional
    void existsById_ShouldReturnFalseForMissingReview() {
        assertFalse(reviewService.existsById(999999),
            "existsById should return false for an unknown ID");
    }

    // -------------------------------------------------------------------------
    // submitNewReview
    // -------------------------------------------------------------------------

    /**
     * Verifies that submitNewReview persists a review with the correct fields
     * and links it to the correct user.
     */
    @Test
    @Transactional
    void submitNewReview_ShouldPersistAndReturnReview() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        ReviewRequest request = new ReviewRequest(
            seededUser.user().getUserId(), testSite.getSiteId(), 4.0, "Good place."
        );

        Reviews result = reviewService.submitNewReview(request);

        assertNotNull(result, "Submitted review should not be null");
        assertNotNull(result.getReviewId(), "Saved review should have a generated ID");
        assertEquals(4.0, result.getRating(), "Rating should match request");
        assertEquals("Good place.", result.getRwComment(), "Comment should match request");
        assertEquals(seededUser.user().getUserId(), result.getUser().getUserId(),
            "Review should be linked to the correct user");
    }

    /**
     * Verifies that submitNewReview throws ResourceNotFoundException when
     * given a user ID that does not exist.
     */
    @Test
    @Transactional
    void submitNewReview_ShouldThrowForUnknownUser() {
        ReviewRequest request = new ReviewRequest(999999, testSite.getSiteId(), 5.0, "Ghost user review.");

        assertThrows(ResourceNotFoundException.class,
            () -> reviewService.submitNewReview(request),
            "Expected exception when user does not exist");
    }

    // -------------------------------------------------------------------------
    // deleteReview
    // -------------------------------------------------------------------------

    /**
     * Verifies that deleteReview removes the review so it can no longer be found.
     */
    @Test
    @Transactional
    void deleteReview_ShouldRemoveFromRepository() {
        Reviews created = createReviewForRandUser(2.0, "Not great.");

        mockSecurityContext(created.getUser());
        reviewService.deleteReview(created.getReviewId());

        assertFalse(reviewService.existsById(created.getReviewId()),
            "Review should no longer exist after deletion");
    }

    /**
     * Verifies that deleting one review does not affect others.
     */
    @Test
    @Transactional
    void deleteReview_ShouldNotAffectOtherReviews() {
        Reviews first  = createReviewForRandUser(4.0, "Good.");
        Reviews second = createReviewForRandUser(5.0, "Great!");

        mockSecurityContext(first.getUser());
        reviewService.deleteReview(first.getReviewId());

        assertTrue(reviewService.existsById(second.getReviewId()),
            "Unrelated review should still exist after deleting another");
    }

    // -------------------------------------------------------------------------
    // Repository: findByUser_UserId
    // -------------------------------------------------------------------------

    /**
     * Verifies that reviews can be retrieved by the user who wrote them.
     */
    @Test
    @Transactional
    void findByUser_UserId_ShouldReturnReviewsForThatUser() {
        SeededUser seededUser = getRandUserAndPassByRole(User.Role.ROLE_USER);
        int userId = seededUser.user().getUserId();

        reviewService.submitNewReview(new ReviewRequest(userId, testSite.getSiteId(), 3.0, "Okay."));
        reviewService.submitNewReview(new ReviewRequest(userId, testSite.getSiteId(), 4.0, "Pretty good."));

        List<Reviews> results = reviewsRepository.findByUser_UserId(userId);

        assertTrue(results.size() >= 2,
            "Expected at least 2 reviews for this user, got: " + results.size());
        assertTrue(results.stream().allMatch(r -> r.getUser().getUserId() == userId),
            "All returned reviews should belong to the same user");
    }

    // -------------------------------------------------------------------------
    // Repository: findByRatingGreaterThanEqual
    // -------------------------------------------------------------------------

    /**
     * Verifies that reviews at or above a given rating threshold are returned,
     * and that reviews below the threshold are excluded.
     */
    @Test
    @Transactional
    void findByRatingGreaterThanEqual_ShouldFilterCorrectly() {
        createReviewForRandUser(5.0, "Perfect!");
        createReviewForRandUser(4.0, "Really good.");
        createReviewForRandUser(2.0, "Not great.");

        List<Reviews> highRated = reviewsRepository.findByRatingGreaterThanEqual(4.0);
        List<Reviews> all       = reviewsRepository.findByRatingGreaterThanEqual(0.0);

        assertTrue(highRated.stream().allMatch(r -> r.getRating() >= 4.0),
            "All results should have a rating >= 4.0");
        assertTrue(all.size() > highRated.size(),
            "Lowering the threshold should return more results");
    }
}