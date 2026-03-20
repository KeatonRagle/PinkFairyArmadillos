package com.pink.pfa.models.datatransfer;

import com.pink.pfa.models.AdoptionSite;

public record AdoptionSiteDTO(
    String name
) {

    public static AdoptionSiteDTO fromEntity(AdoptionSite site) {
        return new AdoptionSiteDTO(
            site.getName()
        );
    }
}
