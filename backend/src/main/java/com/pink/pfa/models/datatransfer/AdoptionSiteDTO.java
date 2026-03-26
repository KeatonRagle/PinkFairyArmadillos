package com.pink.pfa.models.datatransfer;

import com.pink.pfa.models.AdoptionSite;


/**
 * Data Transfer Object representing an {@link AdoptionSite} exposed across the API boundary.
 * <p>
 * Used to decouple the internal {@link AdoptionSite} entity from the API response shape,
 * ensuring only intended fields are serialized and returned to the client.
 */
public record AdoptionSiteDTO(
    Integer siteId,
    String url,
    String name,
    String email,
    String phone,
    Double rating,
    char status
) {
    /**
     * Maps an {@link AdoptionSite} entity to an {@link AdoptionSiteDTO}.
     *
     * @param site the entity to convert
     * @return an {@link AdoptionSiteDTO} populated with the entity's data
     */
    public static AdoptionSiteDTO fromEntity(AdoptionSite site) {
        return new AdoptionSiteDTO(
            site.getSiteId(),
            site.getUrl(),
            site.getName(),
            site.getEmail(),
            site.getPhone(),
            site.getRating(),
            site.getStatus()
        );
    }
}
