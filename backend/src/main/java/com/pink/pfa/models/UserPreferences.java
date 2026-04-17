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
 * JPA entity representing a user preferences record in the database.
 * <p>
 * This class maps directly to the underlying reviews table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store user preferences (trait preferred, value of preference).</li>
 * </ul>
 */
@Data
@Entity
public class UserPreferences {
	
	
	/** Primary key identifier for the review (auto-generated). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pref_id")
	private Integer pref_id;
	
	
	/** Foreign key identifier for the user. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	
	/** Trait user prefers. */
	@Column(name = "pref_trait", nullable = false)
	private String prefTrait;
	
	
	/** Value of trait user prefers. */
	@Column(name = "pref_value", nullable = false)
	private String prefValue;
	
	
	/** Default constructor required by JPA. */
	public Reviews() {}
	
	
	/**
     * Constructs a fully initialized UserPreferences entity.
     *
     * @param prefTrait trait user prefers
	 * @param prefValue value of trait user prefers
     */
	public UserPreferences(String prefTrait, String prefValue) {
		this.prefTrait = prefTrait;
		this.prefValue = prefValue;
	}
}