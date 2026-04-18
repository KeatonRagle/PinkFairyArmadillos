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
	@Column(name = "review_id")
	private Integer review_id;
	
	
	/** Foreign key identifier for the user. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	
	/** Foreign key identifier for the adoptionSite. */
	@ManyToOne
	@JoinColumn(name = "site_id", nullable = false)
	private AdoptionSite site;
	
	
	/** Review's listed rating. */
	@Column(name = "rating", nullable = false)
	private double rating;
	
	
	/** Listed review comment. */
	@Column(name = "rw_comment", nullable = false)
	private String rwComment;
	
	
	/** Listed review date. */
	@Column(name = "rw_date", nullable = false)
	private LocalDate rwDate;
	
	
	/** Default constructor required by JPA. */
	public Reviews() {}
	
	
	/**
     * Constructs a fully initialized Review entity.
     *
     * @param rating center's user-determiend rating
     * @param rwComment user review comment
	 * @param rwDate review date
     */
	public Reviews(double rating, String rwComment, LocalDate rwDate) {
		this.rating = rating;
		this.rwComment = rwComment;
		this.rwDate = rwDate;
	}
}
