package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.Pet;


/**
 * Data access layer for {@link Pet} entities.
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
 public interface PetRepository extends JpaRepository<Pet, Integer> {
	
	
	/** Find all pets by name */
    List<Pet> findByName(String name);

    /** Find all pets of a given type (e.g. "dog", "cat") */
    List<Pet> findByPetType(String petType);

    /** Find all pets at a specific adoption site */
    List<Pet> findBySiteId(Integer siteId);

    /** Find all pets with a given status (e.g. "available", "adopted") */
    List<Pet> findByPetStatus(String petStatus);

    /** Find all pets by breed */
    List<Pet> findByBreed(String breed);
}
