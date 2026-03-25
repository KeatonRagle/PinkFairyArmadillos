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
 * JPA entity representing a submissions record in the database.
 * <p>
 * This class maps directly to the underlying submissions table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store submission information (pet info, pet status).</li>
 * </ul>
 */
@Data
@Entity
public class Submissions {
	
	
	/** Primary key identifier for the submission (auto-generated). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "submission_id")
	private Integer submissionId;
	
	/** Foreign key identifier for the user. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contributor_id", nullable = false)
	private User user;
	
	
	/** Submitted pet's info. */
	@Column(name = "pet_info", nullable = false)
	private String petInfo;
	
	
	/** Submission date. */
	@Column(name = "sb_date", nullable = false)
	private LocalDate sbDate;
	
	
	/** Submitted pet's status. */
	@Column(name = "pet_status", nullable = false)
	private String petStatus;
	
	
	/** Default constructor required by JPA. */
	public Submissions() {}
	
	
	/**
     * Constructs a fully initialized Submissions entity.
     *
     * @param pet_info user submitted pet information
     * @param sb_date date of user submission
	 * @param pet_status status of pet submitted
     */
	public Submissions(String pet_info, LocalDate sbDate, String petStatus) {
		this.petInfo = pet_info;
		this.sbDate = sbDate;
		this.petStatus = petStatus;
	}
}
