package com.pink.pfa.models;

import java.time.LocalDateTime;

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
	@Column(name = "comment_id")
	private Integer commentId;
	
	
	/** Foreign key identifier for the user. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	
	/** Foreign Key identifier for the post. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Posts post;
	
	
	/** Comment's date. */
	@Column(name = "ct_date", nullable = false)
	private LocalDateTime ctDate;
	
	
	/** Comment's comment. */
	@Column(name = "ct_comment", nullable = false)
	private String ctComment;
	
	
	/** Default constructor required by JPA. */
	public Comments() {}
	
	/**
     * Constructs a fully initialized Review entity.
     *
     * @param ctDate date of comment left
     * @param ctComment user-written comment
     */
	public Comments(LocalDateTime ctDate, String ctComment) {
		this.ctDate = ctDate;
		this.ctComment = ctComment;
	}
}
