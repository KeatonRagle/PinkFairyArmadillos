package com.pink.pfa.models.datatransfer;

import java.time.LocalDate;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Reviews;

/**
 * Data Transfer Object representing an {@link AdoptionSite} exposed across the API boundary.
 * <p>
 * Used to decouple the internal {@link AdoptionSite} entity from the API response shape,
 * ensuring only intended fields are serialized and returned to the client.
 */
public record ReviewDTO(
    Integer id,
    Integer userId,
    String username,
    AdoptionSiteDTO site,
    Double rating,
    String comment,
    LocalDate date
) {
    /**
     * Maps an {@link AdoptionSite} entity to an {@link AdoptionSiteDTO}.
     *
     * @param site the entity to convert
     * @return an {@link AdoptionSiteDTO} populated with the entity's data
     */
    public static ReviewDTO fromEntity(Reviews review) {
        return new ReviewDTO(
            review.getReviewId(),
            review.getUser().getUserId(),
            review.getUser().getName(),
            AdoptionSiteDTO.fromEntity(review.getSite()),
            review.getRating(),
            review.getRwComment(),
            review.getRwDate()
        );
    }
}
