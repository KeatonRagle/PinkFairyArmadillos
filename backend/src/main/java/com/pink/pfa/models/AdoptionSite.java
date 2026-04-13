package com.pink.pfa.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 *   <li>Persist optional metadata such as the site's url.</li>
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
	
	
	/** Foreign key identifier for the user. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	
	/** Site's display name (required). */
	@Column(name = "name", nullable = false)
	private String name;
	
	
	/** Site's listed phone number. */
	@Column(name = "phone", nullable = true)
	private String phone;
	
	
	/** Site's listed email. */
	@Column(name = "email", nullable = true)
	private String email;
	
	
	/** Site's user-determined rating. */
	@Column(name = "rating", nullable = true)
	private Double rating;
	
	
	/** Site's url. */
	@Column(name = "url", nullable = false)
    private String url;


	/** Site's approval status. */
	@Column(name = "status", nullable = false)
    private char status = 'P';

	
	/** Site's submission date. */
	@Column(name = "submitted_at", nullable = false)
	private LocalDate submittedAt;

	
	/** Default constructor required by JPA. */
    public AdoptionSite() {}
	
	
	/**
     * Constructs a fully initialized AdoptionSite entity.
     *
     * @param name site's display name
     * @param phone site's phone number
	 * @param email site's email address
     * @param rating user-determined rating
     * @param url optional site metadata
	 * @param status site's approval status
	 * @param submittedAt site's submission date
     */
	public AdoptionSite(String name, String phone, String email,
		double rating, String url, char status, LocalDate submittedAt
	) {
		this.name = name;
		this.phone = phone;
        this.email = email;
		this.rating = rating;
		this.url = url;
		this.status = status;
		this.submittedAt = submittedAt;
	}
}
