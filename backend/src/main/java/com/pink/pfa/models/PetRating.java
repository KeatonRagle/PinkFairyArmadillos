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
 * JPA entity representing a pet rating record in the database.
 * <p>
 * This class maps directly to the underlying petRating table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store pet rating information (start date, end date, etc.).</li>
 * </ul>
 */
@Data
@Entity
public class PetRating {
	
	
	/** Primary key identifier for the pet rating (auto-generated). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rating_id")
	private int ratingId;
	
	/** Foreign key identifier for the user. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	
	/** Foreign key identifier for the pet. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;
	
	
	/** Pet rating's listed rating date. */
	@Column(name = "rating_date", nullable = false)
	private LocalDate ratingDate;
	
	/** Pet's rating. */
	@Column(name = "rating", nullable = false)
	private Double rating;
	
	/** Default constructor required by JPA. */
	public PetRating() {}
	
	/**
     * Constructs a fully initialized PetRating entity.
     *
     * @param ratingDate pet's rating date
     * @param rating pet's rating
     */
	public PetRating(LocalDate ratingDate, double rating) {
		this.ratingDate = ratingDate;
		this.rating = rating;
	}
}
