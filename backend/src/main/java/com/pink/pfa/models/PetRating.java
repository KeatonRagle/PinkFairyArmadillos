package com.pink.pfa.models;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	private int rating_id;
	
	
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
	private LocalDate rating_date;
	
	
	/** Pet's rating. */
	@Column(name = "rating", nullable = false)
	private Double rating;
	
	
	/** Default constructor required by JPA. */
	public PetRating() {
	}
	
	
	/**
     * Constructs a fully initialized PetRating entity.
     *
     * @param rating_date pet's rating date
     * @param rating pet's rating
     */
	public PetRating(LocalDate rating_date, double rating) {
		this.rating_date = rating_date;
		this.rating = rating;
	}
	
	
	/*+++ Getters +++*/
	public LocalDate getRating_date() {
		return rating_date;
	}
	
	public double getRating() {
		return rating;
	}
	
	
	/*+++ Setters +++*/
	public void setRating_date(LocalDate rating_date) {
		this.rating_date = rating_date;
	}
	
	public void setRating(double rating) {
		this.rating = rating;
	}
}
