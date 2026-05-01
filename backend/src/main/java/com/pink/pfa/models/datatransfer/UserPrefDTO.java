package com.pink.pfa.models.datatransfer;


import com.pink.pfa.models.UserPreferences;


/**
 * Data Transfer Object representing a {@link UserPreference} exposed across the API boundary.
 * <p>
 * Used to decouple the internal {@link UserPreference} entity from the API response shape,
 * ensuring only intended fields are serialized and returned to the client.
 */
public record UserPrefDTO(
        Integer id,
        Integer userId,
        String trait,
        String value
) {
    /**
     * Maps a {@link UserPreferences} entity to a {@link UserPrefDTO}.
     *
     * @param pet the entity to convert
     * @return a {@link UserPrefDTO} populated with the entity's data
     */
    public static UserPrefDTO fromEntity(UserPreferences pref) {
        return new UserPrefDTO(
            pref.getPrefId(),
            pref.getUser().getUserId(),
            pref.getPrefTrait().toString(), 
            pref.getPrefValue() 
        );
    }
}
