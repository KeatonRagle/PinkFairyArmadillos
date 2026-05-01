package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.ReviewRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.SiteAlreadyExistsException;
import com.pink.pfa.models.Reviews;
import com.pink.pfa.models.datatransfer.ReviewDTO;
import com.pink.pfa.services.ReviewService;

/**
 * REST controller exposing the {@code /api/reviews} API surface for the Pets for All platform.
 *
 * <p>This controller serves as the primary entry point for review interactions,
 * handling database-backed retrieval of review information. 
 * It is intended to be consumed by the React/Vite frontend or any
 * authorized API client.
 *
 * <p>Use this controller when you need to:
 * <ul>
 *   <li>Fetch all review listings stored in the database</li>
 *   <li>Search for comments by characteristics</li>
 *   <li>Look up a review by its unique post ID</li>
 * </ul>
 *
 * <p>All business logic is delegated to {@link ReviewService}; this controller is
 * responsible only for request mapping, response shaping, and timestamp injection.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewsController {
    @Autowired private ReviewService reviewService; 

    /**
     * Returns all comments currently stored in the database, along with a UTC timestamp.
     *
     * @return a map containing a {@code Comments} list and a {@code Timestamp} string
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<ReviewDTO>> getAllReviews(
        @RequestParam(required = false) Double minRating
    ) {
        try {
            return ResponseEntity.ok().body(reviewService.findAll(minRating));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
 
    /**
     * Returns a review by its ID, along with a UTC timestamp.
     *
     * @param id the unique identifier of the review
     * @return a map containing the matched {@code review} and a {@code Timestamp} string
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(
        @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok().body(reviewService.findById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Emplaces a review by its ID, along with a UTC timestamp.
     *
     * @param id the unique identifier of the review
     * @return a map containing the matched {@code review} and a {@code Timestamp} string
     */
    @PostMapping("/submitReview")
    public ResponseEntity<ReviewDTO> createReview(
        @RequestBody ReviewRequest review
    ) {
        try { 
            return ResponseEntity.ok().body(reviewService.submitNewReview(review));
        } catch (SiteAlreadyExistsException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deletes a review by its ID, returning the result of the operations with a UTC timestamp.
     *
     * @param id the unique identifier of the review
     * @return a map containing the response from the operation with a {@code Timestamp} string
     */
    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id) {
        if (!reviewService.existsById(id)) {
            return ResponseEntity.notFound().build();   
        }

        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
