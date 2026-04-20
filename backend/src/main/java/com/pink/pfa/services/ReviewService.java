package com.pink.pfa.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pink.pfa.controllers.requests.ReviewRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.Reviews;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.repos.AdoptionSiteRepository;
import com.pink.pfa.repos.ReviewsRepository;
import com.pink.pfa.repos.UserRepository;

@Service
public class ReviewService {
    @Autowired private ReviewsRepository reviewRepository;
    @Autowired private AdoptionSiteRepository siteRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private UserService userService;

    public List<Reviews> findAll(Double minRating) { 
        if (minRating != null) {
            return reviewRepository.findByRatingGreaterThanEqual(minRating)
                .stream()
                .toList();
        }
        
        return reviewRepository.findAll()
            .stream()
            .toList(); 
    } 

    public List<Reviews> findAllByUserId(Integer userId) { 
        return reviewRepository.findByUser_UserId(userId)
            .stream()
            .toList(); 
    }

    public List<Reviews> findBySiteId(Integer siteId) { 
        return reviewRepository.findBySite_SiteId(siteId)
            .stream()
            .toList(); 
    }

    public List<Reviews> findByRatingGreaterThanEqual(Double rating) {
        return reviewRepository.findByRatingGreaterThanEqual(rating)
            .stream()
            .toList();
    }

    public Reviews findById(Integer id) { 
        return reviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review", id));
    }

    public Boolean existsById(Integer id) { 
        return reviewRepository.existsById(id);
    }

    public void deleteReview(Integer id) { 
        UserDTO user = userService.findByJWT();
        Reviews review = reviewRepository.findByReviewIdAndUser_UserId(id, user.id())
            .orElseThrow(() -> new RuntimeException("Review not found or not yours"));
        reviewRepository.delete(review);
    }

    public Reviews submitNewReview(ReviewRequest request) {
        Reviews review = new Reviews(request.rating(), request.comment());
        review.setUser(userRepository.findById(request.userID())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.userID()))
        );

        review.setSite(siteRepository.findById(request.siteId())
            .orElseThrow(() -> new ResourceNotFoundException("Site", request.siteId()))
        );

        Reviews savedPost = reviewRepository.save(review);
        return savedPost;
    }
}
