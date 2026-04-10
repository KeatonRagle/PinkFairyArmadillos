package com.pink.pfa.models.datatransfer;

import java.time.LocalDateTime;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Comments;
import com.pink.pfa.models.Posts;

/**
 * Data Transfer Object representing an {@link AdoptionSite} exposed across the API boundary.
 * <p>
 * Used to decouple the internal {@link AdoptionSite} entity from the API response shape,
 * ensuring only intended fields are serialized and returned to the client.
 */
public record CommentDTO(
    Posts post,
    Integer commentID,
    Integer userID,
    String username,
    LocalDateTime date,
    String comment
) {
    /**
     * Maps an {@link AdoptionSite} entity to an {@link AdoptionSiteDTO}.
     *
     * @param site the entity to convert
     * @return an {@link AdoptionSiteDTO} populated with the entity's data
     */
    public static CommentDTO fromEntity(Comments comment) {
        return new CommentDTO(
            comment.getPost(),
            comment.getCommentId(),
            comment.getUser().getUserId(),
            comment.getUser().getName(),
            comment.getCtDate(),
            comment.getCtComment()
        );
    }
}
