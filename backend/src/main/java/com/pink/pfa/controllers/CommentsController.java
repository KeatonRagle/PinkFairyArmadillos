package com.pink.pfa.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.datatransfer.CommentDTO;
import com.pink.pfa.services.CommentsService;

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
@RequestMapping("/api/comments")
public class CommentsController {

    private final CommentsService commentsService;

    public CommentsController (CommentsService commentsService) {
        this.commentsService = commentsService;
    }
 

    /**
     * Returns all comments currently stored in the database, along with a UTC timestamp.
     *
     * @return a map containing a {@code Comments} list and a {@code Timestamp} string
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        try {
            return ResponseEntity.ok().body(commentsService.findAll());
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
    public ResponseEntity<CommentDTO> getCommentById(
        @PathVariable Integer id
    ) {
        try {
            return ResponseEntity.ok().body(commentsService.findById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
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
    public ResponseEntity<Void> deleteComment(
        @PathVariable Integer id
    ) {
        if (!commentsService.existsById(id)) {
            return ResponseEntity.notFound().build();   
        }

        commentsService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
