package com.pink.pfa.models;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;


/**
 * JPA entity representing a posts record in the database.
 * <p>
 * This class maps directly to the underlying Posts table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store post information (content).</li>
 *   <li>Persist optional metadata such as posting date.</li>
 * </ul>
 */
@Data
@Entity
public class Posts {
	
	
	/** Primary key identifier for the posts (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
    private Integer postId;
	
	
	/** Foreign key identifier for the user. */
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	
	/** Post's date with time. */
	@Column(name = "post_date", nullable = false)
    private LocalDate postDate;
	
	
	/** Post's content. */
	@Column(name = "post_content", nullable = false)
	private String postContent;
	
	
	/** Default constructor required by JPA. */
	public Posts() {
	}
	
	/**
     * Constructs a fully initialized Review entity.
     *
     * @param postDate date and time of post left
     * @param postContent user-written post content
     */
	public Posts(LocalDate postDate, String postContent) {
		this.postDate = postDate;
		this.postContent = postContent;
	}
}