package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.Submissions;


/**
 * Data access layer for {@link Submissions} entities.
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
public interface SubmissionsRepository extends JpaRepository<Submissions, Integer> {

    
	/** Finds all submissions made by a specific contributor */
	List<Submissions> findByContributorUserId(Integer userId);
    
	/** Finds all submissions with a given status (e.g. "pending", "approved") */
	List<Submissions> findByPetStatus(String petStatus);
}