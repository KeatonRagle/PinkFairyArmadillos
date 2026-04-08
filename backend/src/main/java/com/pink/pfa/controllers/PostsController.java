package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.PostRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.SiteAlreadyExistsException;
import com.pink.pfa.models.datatransfer.PostDTO;
import com.pink.pfa.services.CommentsService;
import com.pink.pfa.services.PostsService;

/**
 * REST controller exposing the {@code /api/comments} API surface for the Pets for All platform.
 *
 * <p>This controller serves as the primary entry point for comment interactions,
 * handling database-backed retrieval of comment information. 
 * It is intended to be consumed by the React/Vite frontend or any
 * authorized API client.
 *
 * <p>Use this controller when you need to:
 * <ul>
 *   <li>Fetch all comment listings stored in the database</li>
 *   <li>Search for comments by characteristics</li>
 *   <li>Look up a comment by its unique post ID</li>
 * </ul>
 *
 * <p>All business logic is delegated to {@link CommentsService}; this controller is
 * responsible only for request mapping, response shaping, and timestamp injection.
 */
@RestController
@RequestMapping("/api/posts")
public class PostsController {
    @Autowired private PostsService postsService; 

    /**
     * Returns all comments currently stored in the database, along with a UTC timestamp.
     *
     * @return a map containing a {@code Comments} list and a {@code Timestamp} string
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<PostDTO>> getAllComments() {
        try {
            return ResponseEntity.ok().body(postsService.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
 
    /**
     * Returns a comment by its ID, along with a UTC timestamp.
     *
     * @param id the unique identifier of the comment
     * @return a map containing the matched {@code Comment} and a {@code Timestamp} string
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getCommentById(
        @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok().body(postsService.findById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Emplaces a comment by its ID, along with a UTC timestamp.
     *
     * @param id the unique identifier of the comment
     * @return a map containing the matched {@code Comment} and a {@code Timestamp} string
     */
    @PostMapping("/submitPost")
    public ResponseEntity<PostDTO> createPost(
        @RequestBody PostRequest post
    ) {
        try { 
            return ResponseEntity.ok().body(postsService.submitNewPost(post));
        } catch (SiteAlreadyExistsException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deletes a comment by its ID, returning the result of the operations with a UTC timestamp.
     *
     * @param id the unique identifier of the comment
     * @return a map containing the response from the operation with a {@code Timestamp} string
     */
    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> deletePost(
        @PathVariable Integer id
    ) {
        if (!postsService.existsById(id)) {
            return ResponseEntity.notFound().build();   
        }

        postsService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
