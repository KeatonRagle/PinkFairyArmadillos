package com.pink.pfa.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.User;


/**
 * Data access layer for {@link User} entities.
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
public interface UserRepository extends JpaRepository<User, Integer> {


    /**
     * Finds all users with the given name.
     * Spring automatically derives the query from the method name.
     *
     * @param name name to search for
     * @return list of {@link User} entities matching the name
     */
    List<User> findByName(String name);


    /**
     * Finds a single user by email.
     * Used primarily for authentication and user lookup.
     *
     * @param email unique email identifier
     * @return {@link User} if found, otherwise null
     */
    User findByEmail(String email);

    
    //User findById(Integer user_id);
}
