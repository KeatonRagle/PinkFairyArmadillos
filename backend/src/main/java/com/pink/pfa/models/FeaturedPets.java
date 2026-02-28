package com.pink.pfa.models;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


/**
 * JPA entity representing a featured pet record in the database.
 * <p>
 * This class maps directly to the underlying featuredPets table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store featured pets information (start date, end date, etc.).</li>
 * </ul>
 */
@Data
@Entity
public class FeaturedPets {
	
	
	/** Primary key identifier for the featured pet (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pet_id;
	
	
	/** Foreign key identifier for the pet. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;
	
	
	/** Featured pet's listed start date. */
	@Column(name = "start_date", nullable = false)
	private LocalDate start_date;
	
	
	/** Featured pet's listed end date. */
	@Column(name = "end_date", nullable = false)
	private LocalDate end_date;
	
	
	/** Featured pet's reason for being featured. */
	@Column(name = "reason", nullable = false)
	private String reason;
	
	
	/** Default constructor required by JPA. */
	public FeaturedPets() {
	}
	
	
	/**
     * Constructs a fully initialized FeaturedPets entity.
     *
     * @param start_date pet's feature start date
     * @param end_date pet's feature end date
     * @param reason admin/contributor added reason
     */
	public FeaturedPets(LocalDate start_date, LocalDate end_date, String reason) {
		this.start_date = start_date;
		this.end_date = end_date;
		this.reason = reason;
	}
	
	
	/*+++ Getters +++*/
	public Integer getPet_id() {
		return pet_id;
	}
	
	public LocalDate getStart_date() {
		return start_date;
	}
	
	public LocalDate getEnd_date() {
		return end_date;
	}
	
	public String getReason() {
		return reason;
	}
	
	
	/*+++ Setters +++*/
	public void setStart_date(LocalDate start_date) {
		this.start_date = start_date;
	}
	
	public void setEnd_date(LocalDate end_date) {
		this.end_date = end_date;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
}