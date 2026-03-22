package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;


/**
 * Data access layer for {@link AdoptionSite} entities.
 * <p>
 * Extends {@link JpaRepository} to provide built-in CRUD operations.
 * Spring Data JPA generates the implementation at runtime.
 */
@Repository
public interface AdoptionSiteRepository extends JpaRepository<AdoptionSite, Integer> {
    /**
     * Finds all pets with the given name.
     * Spring automatically derives the query from the method name.
     *
     * @param name Name to search for
     * @return list of {@link Pet} entities matching the name
     */
    List<AdoptionSite> findByName(String name);

    /**
     * Finds the adoption site with the given name.
     * Spring automatically derives the query from the method name.
     *
     * @param siteId ID to search for
     * @return the site matching the ID
     */
    Optional<AdoptionSite> findBySiteId(int siteId);
}
