package com.pink.pfa.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


/**
 * JPA entity representing an adoption site record in the database.
 * <p>
 * This class maps directly to the underlying adoptionSite table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store site information (name, contact information, etc.).</li>
 *   <li>Persist optional metadata such as the site's location.</li>
 * </ul>
 */
@Data
@Entity
public class AdoptionSite {
	
	
	/** Primary key identifier for the adoption site (auto-generated). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "site_id")
	private Integer siteId;
	
	
	/** Site's display name (required). */
	@Column(name = "name", nullable = false)
	private String name;
	
	
	/** Site's listed contact info. */
	@Column(name = "contact_info", nullable = false)
	private String contactInfo;
	
	
	/** Site's user-determined rating. */
	@Column(name = "rating", nullable = true)
	private double rating;
	
	
	/** Optional location metadata associated with the site. */
    private String location;
	
	
	/** Default constructor required by JPA. */
    public AdoptionSite() {}
	
	
	/**
     * Constructs a fully initialized AdoptionSite entity.
     *
     * @param name site's display name
     * @param contactInfo site's contact info
     * @param rating user-determined rating
     * @param location optional location metadata
     */
	public AdoptionSite(String name, String contactInfo, double rating, String location) {
		this.name = name;
		this.contactInfo = contactInfo;
		this.rating = rating;
		this.location = location;
	}
}
