package com.pink.pfa.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;


/**
 * JPA entity representing a user record in the database.
 * <p>
 * This class maps directly to the underlying User table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store user identity information (name, email).</li>
 *   <li>Store authentication credentials (hashed password).</li>
 *   <li>Define authorization role (USER or ADMIN).</li>
 *   <li>Persist optional metadata such as location.</li>
 * </ul>
 *
 * Security Notes:
 * <ul>
 *   <li>The password field should always store a hashed value (never plain text).</li>
 *   <li>The role field controls authorization and is stored as a String
 *       via {@link jakarta.persistence.EnumType#STRING} for readability
 *       and safety against enum reordering.</li>
 * </ul>
 */
@Data
@Entity
public class UserPref {


    /** Primary key identifier for the user (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
	
    /** User s display name (required). */
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

    /** Preference type (say, breed, etc) */
    public enum Preference { BREED, GENDER, AGE_MIN, AGE_MAX, SIZE }
    @Column(name = "preference_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Preference preferenceType;

    /** Preference type s value (say, Labrador, etc) */
    @Column(name = "value", nullable = false)
    private String value;	
	
	/** User s banned status.  */
	@Column(name = "created_at", nullable = false)
	private LocalDate createAt = LocalDate.now();

    /** Default constructor required by JPA. */
    public UserPref() {}

    /**
     * Constructs a fully initialized User entity.
     *
     * @param name user s display name
     * @param email unique login email
     * @param password hashed password
     * @param role authorization role
     * @param location optional location metadata
	 * @param isBanned profile ban status
	 * @param requestedContributor user requested contributor status
     */
    public UserPref(Preference preferenceType, String value) {
        this.preferenceType = preferenceType;
        this.value = value;
    }
}
