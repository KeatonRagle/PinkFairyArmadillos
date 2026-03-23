package com.pink.pfa.models.datatransfer;

import com.pink.pfa.models.AdoptionSite;

public record AdoptionSiteDTO(
    String url,
    String name,
    String email,
    String phone
) {

    public static AdoptionSiteDTO fromEntity(AdoptionSite site) {
        return new AdoptionSiteDTO(
            site.getUrl(),
            site.getName(),
            site.getEmail(),
            site.getPhone()
        );
    }
}
