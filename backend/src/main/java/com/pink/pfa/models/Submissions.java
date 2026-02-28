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
	private Integer submission_id;
	
	
	/** Foreign key identifier for the user. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contributor_id", nullable = false)
	private User user;
	
	
	/** Submitted pet's info. */
	@Column(name = "pet_info", nullable = false)
	private String pet_info;
	
	
	/** Submission date. */
	@Column(name = "sb_date", nullable = false)
	private LocalDate sb_date;
	
	
	/** Submitted pet's status. */
	@Column(name = "pet_status", nullable = false)
	private String pet_status;
	
	
	/** Default constructor required by JPA. */
	public Submissions() {
	}
	
	
	/**
     * Constructs a fully initialized Submissions entity.
     *
     * @param pet_info user submitted pet information
     * @param sb_date date of user submission
	 * @param pet_status status of pet submitted
     */
	public Submissions(String pet_info, LocalDate sb_date, String pet_status) {
		this.pet_info = pet_info;
		this.sb_date = sb_date;
		this.pet_status = pet_status;
	}
	
	
	/*+++ Getters +++*/
	public String getPet_info() {
		return pet_info;
	}
	
	public LocalDate getSb_date() {
		return sb_date;
	}
	
	public String getPet_status() {
		return pet_status;
	}
	
	
	/*+++ Setters +++*/
	public void setPet_info(String pet_info) {
		this.pet_info = pet_info;
	}
	
	public void setSb_date(LocalDate sb_date) {
		this.sb_date = sb_date;
	}
	
	public void setPet_status(String pet_status) {
		this.pet_status = pet_status;
	}
}
