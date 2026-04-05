package com.pink.pfa.repos;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.Posts;


/**
 * Data access layer for {@link Posts} entities.
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
public interface PostsRepository extends JpaRepository<Posts, Integer>{
	/**
	 * Finds all user-generated posts with the given post ID.
	 * Spring automatically derives the query from the method name.
	 *
	 * @param postId post ID to filter for
	 * @return list of {@link Posts} entities matched by the post's ID
	 */
	List<Posts> findByPostId(Integer postId);
	
	/**
	 * Finds all user-generated posts with the given user ID.
	 * Spring automatically derives the query from the method name.
	 *
	 * @param userId user ID to filter for
	 * @return list of {@link Posts} entities matched by the user's ID
	 */
	List<Posts> findByUser_UserId(Integer userId);
	
	/**
	 * Finds all user-generated posts with the given date and time.
	 * Spring automatically derives the query from the method name.
	 *
	 * @param postDate post date to filter for
	 * @return list of {@link Posts} entities matched by the post's date
	 */
	List<Posts> findByPostDate(LocalDate postDate);
}