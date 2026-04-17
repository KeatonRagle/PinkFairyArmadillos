package com.pink.pfa.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.UserPreferences;


/**
 * Data access layer for {@link User Preferences} entities.
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
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Integer> {
	
	
	/** Finds all user preferences by user. */
	List<UserPreference> findByUser(User user);
    
	/** Finds all users that prefer a certain trait. */
	Optional<UserPreference> findByUserAndPrefTrait(User user, String prefTrait);
    
	/** Finds all users that prefer a certain value. */
	List<UserPreference> findByUserAndPrefValue(User user, String prefValue);
    
	/** Here to prevent duplicate columns. */
	void deleteByUserAndPrefTrait(User user, String prefTrait);
}
