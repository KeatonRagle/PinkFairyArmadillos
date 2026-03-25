package com.pink.pfa.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
	@Column(name = "pet_id")
    private Integer petId;
	
	
	/** Foreign key identifier for the pet. */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;
	
	
	/** Featured pet's listed start date. */
	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;
	
	
	/** Featured pet's listed end date. */
	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;
	
	
	/** Featured pet's reason for being featured. */
	@Column(name = "reason", nullable = false)
	private String reason;
	
	
	/** Default constructor required by JPA. */
	public FeaturedPets() {}
	
	/**
     * Constructs a fully initialized FeaturedPets entity.
     *
     * @param startDate pet's feature start date
     * @param endDate pet's feature end date
     * @param reason admin/contributor added reason
     */
	public FeaturedPets(LocalDate startDate, LocalDate endDate, String reason) {
		this.startDate = startDate;
		this.startDate = endDate;
		this.reason = reason;
	}
}
