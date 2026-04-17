package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.UserPref;


/**
 * Data access layer for {@link UserPref} entities.
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
public interface UserPrefRepository extends JpaRepository<UserPref, Integer> {
	/** Finds all user pref objects attached to a given user */
	List<UserPref> findByUser_UserId(Integer userId);

	/** Finds a single user pref object attached to a given user */
	Optional<UserPref> findByIdAndUser_UserId(Integer id, Integer userId);

	/** Deletes a single user pref object attached to a given user */
	void deleteByIdAndUser_UserId(Integer id, Integer userId);
}
