package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.FeaturedPets;


/**
 * Data access layer for {@link FeaturedPets} entities.
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
public interface FeaturedPetsRepository extends JpaRepository<FeaturedPets, Integer> {

    
	/** Finds all featured pets whose feature window falls between two dates */
	List<FeaturedPets> findByStartDateBetween(LocalDate start, LocalDate end);
    
	/** Finds all featured pets that are still active (end date is in the future) */
	List<FeaturedPets> findByEndDateAfter(LocalDate date);
}
