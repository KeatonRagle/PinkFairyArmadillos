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
 * JPA entity representing an adoption center record in the database.
 * <p>
 * This class maps directly to the underlying adoptionCenter table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store center information (name, contact information, etc.).</li>
 *   <li>Persist optional metadata such as the center's location.</li>
 * </ul>
 */
@Data
@Entity
public class AdoptionCenter {
	
	
	/** Primary key identifier for the adoption center (auto-generated). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer center_id;
	
	
	/** Center's display name (required). */
	@Column(name = "name", nullable = false)
	private String name;
	
	
	/** Center's listed contact info. */
	@Column(name = "contact information", nullable = false)
	private String contact_info;
	
	
	/** Center's user-determined rating. */
	@Column(name = "rating", nullable = true)
	private double rating;
	
	
	/** Optional location metadata associated with the center. */
    private String location;
	
	
	/** Default constructor required by JPA. */
    public AdoptionCenter() {
    }
	
	
	/**
     * Constructs a fully initialized AdoptionCenter entity.
     *
     * @param name center's display name
     * @param contact_info center's contact info
     * @param rating user-determined rating
     * @param location optional location metadata
     */
	public AdoptionCenter(String name, String contact_info, double rating, String location) {
		this.name = name;
		this.contact_info = contact_info;
		this.rating = rating;
		this.location = location;
	}
	
	
	/*+++ Getters +++*/
	public Integers getCenter_id() {
		return center_id;
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