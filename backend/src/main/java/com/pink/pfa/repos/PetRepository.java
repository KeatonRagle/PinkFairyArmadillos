package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Finds all pets with the given site ID
     * Spring automatically derives the query from the method name.
     *
     * @param siteID site ID to filter for
     * @return list of {@link Pet} entities matched by the site's ID
     */
    List<Pet> findBySite_SiteId(Integer siteId);

     /**
     * Finds all pets that do not have a given status
     * Spring automatically derives the query from the method name.
     *
     * @param petStatus status to filter by
     * @return list of {@link Pet} entities currently active
     */
    List<Pet> findByPetStatusNot(String petStatus);

    @Query(value = "SELECT * FROM pet WHERE pet_status <> :status AND pet_type = :type ORDER BY RAND() LIMIT 1", nativeQuery=true)
    Optional<Pet> findByPetStatusNotAndTypeRand(@Param("type") String type, @Param("status") String petStatus);
}
