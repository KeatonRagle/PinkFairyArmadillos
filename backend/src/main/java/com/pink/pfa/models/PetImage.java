package com.pink.pfa.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * JPA entity representing a pet image record in the database.
 * <p>
 * This class maps directly to the underlying Pet Image table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store pet image information (url).</li>
 *   <li>Store relation to pet (pet_id).</li>
 * </ul>
 */
 @Data
 @Entity
 public class PetImage {
	
	
	/** Primary key identifier for the image (auto-generated). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "image_id")
	private Integer imageId;
	
	
	/** Foreign key identifier for the pet. */
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;
	
	
	/** Pet image url (required). */
    @Column(name = "image_url", nullable = false)
    private String imageUrl;


	/** Default constructor required by JPA. */
	public PetImage() {
	}
	
	
	/**
     * Constructs a fully initialized PetImage entity.
     *
     * @param imageUrl url of the pet image
     */
	public PetImage(String imageUrl
	) {
		this.imageUrl = imageUrl;
	}
 }
