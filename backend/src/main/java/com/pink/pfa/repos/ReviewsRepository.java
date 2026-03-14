package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.Reviews;


/**
 * Data access layer for {@link Reviews} entities.
 * <p>
 * This interface extends {@link JpaRepository}, which provides built-in
 * CRUD operations such as:
 * <ul>
 *   <li>save()</li>
 *   <li>findById()</li>
 *   <li>findAll()</li>
 *   <li>deleteById()</li>
 * </ul>
 *
 * Spring Data JPA automatically generates the implementation at runtime.
 * No manual SQL or implementation class is required.
 *
 * Custom query methods defined here follow Spring Data's method naming
 * conventions, allowing query logic to be derived directly from method names.
 */
@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Integer> {
	
	
	/** Finds all reviews for a specific adoption site */
    List<Reviews> findByAdoptionSiteSiteId(Integer siteId);
    
	/** Finds all reviews written by a specific user */
	List<Reviews> findByUserUserId(Integer userId);
    
	/** Finds all reviews at or above a minimum rating threshold */
	List<Reviews> findByRatingGreaterThanEqual(Double rating);
}