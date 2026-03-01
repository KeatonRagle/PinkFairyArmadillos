package com.pink.pfa.repos;

import java.util.List;

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
public interface PetRepository extends JpaRepository<Pet, Integer>{

    /**
     * Finds all pets with the given name.
     * Spring automatically derives the query from the method name.
     *
     * @param name name to search for
     * @return list of {@link Pet} entities matching the name
     */
    List<Pet> findByName(String name);
    
}
