package com.pink.pfa.models.datatransfer;

import java.time.LocalDateTime;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Posts;

/**
 * Data Transfer Object representing an {@link AdoptionSite} exposed across the API boundary.
 * <p>
 * Used to decouple the internal {@link AdoptionSite} entity from the API response shape,
 * ensuring only intended fields are serialized and returned to the client.
 */
public record PostDTO(
    Integer postID,
    Integer userID,
    String username,
    LocalDateTime date,
    String content
) {
    /**
     * Maps an {@link AdoptionSite} entity to an {@link AdoptionSiteDTO}.
     *
     * @param site the entity to convert
     * @return an {@link AdoptionSiteDTO} populated with the entity's data
     */
    public static PostDTO fromEntity(Posts post) {
        return new PostDTO(
            post.getPostId(),
            post.getUser().getUserId(),
            post.getUser().getName(),
            post.getPostDate(),
            post.getPostContent()
        );
    }
}
