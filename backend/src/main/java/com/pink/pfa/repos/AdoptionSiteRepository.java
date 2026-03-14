package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.AdoptionSite;


/**
 * Data access layer for {@link AdoptionSite} entities.
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
public interface AdoptionSiteRepository extends JpaRepository<AdoptionSite, Integer> {

    
	/** Finds a single adoption site by its unique contact info (e.g. email or phone) */
	Optional<AdoptionSite> findByContactInfo(String contactInfo);
	
	/** Finds all adoption sites in a given location */
    List<AdoptionSite> findByLocation(String location);
	
	/** Finds all adoption sites at or above a minimum rating threshold */
    List<AdoptionSite> findByRatingGreaterThanEqual(Double rating);
}
