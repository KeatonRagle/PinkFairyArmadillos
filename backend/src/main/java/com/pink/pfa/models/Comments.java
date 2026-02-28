package com.pink.pfa.models;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


/**
 * JPA entity representing a comments record in the database.
 * <p>
 * This class maps directly to the underlying comments table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store comments information (comment date, comment).</li>
 * </ul>
 */
@Data
@Entity
public class Comments {
	
	
	/** Primary key identifier for the review (auto-generated). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer comment_id;
	
	
	/** Foreign key identifier for the user. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	
	/** Comment's date. */
	@Column(name = "comment_date", nullable = false)
	private LocalDate ct_date;
	
	
	/** Comment's comment. */
	@Column(name = "comment", nullable = false)
	private String ct_comment;
	
	
	/** Default constructor required by JPA. */
	public Comments() {
	}
	
	
	/**
     * Constructs a fully initialized Review entity.
     *
     * @param ct_date date of comment left
     * @param ct_comment user-written comment
     */
	public Comments(LocalDate ct_date, String ct_comment) {
		this.ct_date = ct_date;
		this.ct_comment = ct_comment;
	}
	
	
	/*+++ Getters +++*/
	public LocalDate getCt_date() {
		return ct_date;
	}
	
	public String getCt_comment() {
		return ct_comment;
	}
	
	
	/*+++ Setters +++*/
	public void setCt_date(LocalDate ct_date) {
		this.ct_date = ct_date;
	}
	
	public void setCt_comment(String ct_comment) {
		this.ct_comment = ct_comment;
	}
}