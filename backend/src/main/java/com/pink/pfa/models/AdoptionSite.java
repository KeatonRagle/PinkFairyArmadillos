package com.pink.pfa.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	private Integer site_id;
	
	
	/** Site's display name (required). */
	@Column(name = "name", nullable = false)
	private String name;
	
	
	/** Site's listed contact info. */
	@Column(name = "contact_information", nullable = false)
	private String contact_info;
	
	
	/** Site's user-determined rating. */
	@Column(name = "rating", nullable = true)
	private double rating;
	
	
	/** Optional location metadata associated with the site. */
    private String location;
	
	
	/** Default constructor required by JPA. */
    public AdoptionSite() {
    }
	
	
	/**
     * Constructs a fully initialized AdoptionSite entity.
     *
     * @param name site's display name
     * @param contact_info site's contact info
     * @param rating user-determined rating
     * @param location optional location metadata
     */
	public AdoptionSite(String name, String contact_info, double rating, String location) {
		this.name = name;
		this.contact_info = contact_info;
		this.rating = rating;
		this.location = location;
	}
	
	
	/*+++ Getters +++*/
	public Integer getSite_id() {
		return site_id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getContact_info() {
		return contact_info;
	}
	
	public double getRating() {
		return rating;
	}
	
	
	/*+++ Setters +++*/
	public void setName(String name) {
		this.name = name;
	}
	
	public void setContact_info(String contact_info) {
		this.contact_info = contact_info;
	}
	
	public void setRating(double rating) {
		this.rating = rating;
	}
}