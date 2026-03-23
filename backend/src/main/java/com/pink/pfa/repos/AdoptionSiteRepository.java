package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.AdoptionSite;


/**
 * Data access layer for {@link AdoptionSite} entities.
 * <p>
 * Extends {@link JpaRepository} to provide built-in CRUD operations.
 * Spring Data JPA generates all query implementations at runtime from method names.
 */
@Repository
public interface AdoptionSiteRepository extends JpaRepository<AdoptionSite, Integer> {
    /**
     * Returns all adoption sites matching the given name.
     *
     * @param name name to search for
     * @return list of matching {@link AdoptionSite} entities
     */
    List<AdoptionSite> findByName(String name);

    /**
     * Returns the adoption site with the given ID, if it exists.
     *
     * @param siteId ID to search for
     * @return an {@link Optional} containing the matching site, or empty if not found
     */
    Optional<AdoptionSite> findBySiteId(int siteId);

    /**
     * Returns whether an adoption site with the given URL already exists.
     *
     * @param url URL to check
     * @return {@code true} if a site with the URL exists; otherwise {@code false}
     */
    Boolean existsByUrl(String url);

    /**
     * Returns all adoption sites with the given status.
     * Expected values: {@code 'P'} (Pending), {@code 'A'} (Approved), {@code 'D'} (Denied).
     *
     * @param status status character to filter by
     * @return list of {@link AdoptionSite} entities matching the status
     */
    List<AdoptionSite> findByStatus(char status);
}
