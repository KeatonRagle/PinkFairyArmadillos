package com.pink.pfa.models;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


/**
 * JPA entity representing a review record in the database.
 * <p>
 * This class maps directly to the underlying reviews table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store review information (rating, review comment, review date).</li>
 * </ul>
 */
@Data
@Entity
public class Reviews {
	
	
	/** Primary key identifier for the review (auto-generated). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer review_id;
	
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	
	@ManyToOne
	@JoinColumn(name = "center_id")
	private AdoptionCenter center;
	
	
	/** Review's listed rating. */
	@Column(name = "rating", nullable = false)
	private double rating;
	
	
	/** Listed review date. */
	@Column(name = "review comment", nullable = false)
	private String rw_comment;
	
	
	/** review date. */
	@Column(name = "review date", nullable = false)
	private LocalDate rw_date;
	
	
	/** Default constructor required by JPA. */
	public Reviews() {
	}
	
	
	/**
     * Constructs a fully initialized Review entity.
     *
     * @param rating center's user-determiend rating
     * @param rw_comment user review comment
	 * @param rw_date review date
     */
	public Reviews(double rating, String rw_comment, LocalDate rw_date) {
		this.rating = rating;
		this.rw_comment = rw_comment;
		this.rw_date = rw_date;
	}
	
	
	/*+++ Getters +++*/
	public double getRating() {
		return rating;
	}
	
	public String getRw_comment() {
		return rw_comment;
	}
	
	public LocalDate getRw_date() {
		return rw_date;
	}
	
	
	/*+++ Setters +++*/
	public void setRating(double rating) {
		this.rating = rating;
	}
	
	public void setRw_comment(String rw_comment) {
		this.rw_comment = rw_comment;
	}
	
	public voi setRw_date(LocalDate rw_date) {
		this.rw_date = rw_date;
	}
}