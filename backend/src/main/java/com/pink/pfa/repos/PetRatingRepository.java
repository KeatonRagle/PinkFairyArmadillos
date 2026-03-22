package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.PetRating;


/**
 * Data access layer for {@link PetRating} entities.
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
public interface PetRatingRepository extends JpaRepository<PetRating, Integer> {

    
	/** Finds all ratings submitted for a specific pet */
	List<PetRating> findByPetPetId(Integer petId);
	
	/** Finds all ratings submitted by a specific user */
    List<PetRating> findByUserUserId(Integer userId);
	
	/** Finds all ratings at or above a minimum rating threshold */
    List<PetRating> findByRatingGreaterThanEqual(Double rating);
}