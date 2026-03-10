package com.pink.pfa.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.AdoptionSite;


/**
 * Data access layer for {@link AdoptionSite} entities.
 * <p>
 * Extends {@link JpaRepository} to provide built-in CRUD operations.
 * Spring Data JPA generates the implementation at runtime.
 */
@Repository
public interface AdoptionSiteRepository extends JpaRepository<AdoptionSite, Integer> {
}
