package com.pink.pfa.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * JPA entity representing a pet record in the database.
 * <p>
 * This class maps directly to the underlying Pet table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store pet information (name, breed, age, gender).</li>
 *   <li>Store clerical information (pet type, price, pet status).</li>
 *   <li>Persist optional metadata such as pet location.</li>
 * </ul>
 *
 * Security Notes:
 * <ul>
 *   <li>No passwords present within the Pet class but IDs
 *		 will be kept safe via JPA's persistence systems</li>
 * </ul>
 */
 @Data
 @Entity
 @Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "breed", "pet_type"})
 })
 public class Pet {
	
	
	/** Primary key identifier for the pet (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pet_id")
    private Integer petId;
	
	
	/** Foreign key identifier for the adoptionSite. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id", nullable = false)
	private AdoptionSite site;

	/** One to many list of linked petImages. */
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PetImage> secondaryImages = new ArrayList<>();
	
	/** Pet's display name (required). */
    @Column(name = "name", nullable = false)
    private String name;
	
	
	/** Pet's breed. */
	@Column(name = "breed", nullable = false)
	private String breed;
	
	
	/** Pet's age. */
	@Column(name = "age", nullable = false)
	private Integer age;
	
	
	/** Pet's gender. */
	@Column(name = "gender", nullable = false)
	private char gender;
	
	
	/** Type of pet. */
	@Column(name = "pet_type", nullable = false)
	private String petType;
	
	
	/** Location of pet. */
	@Column(name = "location", nullable = false)
	private String location;
	
	
	/** Pet's price. */
	@Column(name = "price", nullable = true)
	private Double price;
	
	
	/** Pet's status. */
	@Column(name = "pet_status", nullable = false)
	private String petStatus;


	/** Pet's image url. */
	@Column(name = "img_url", nullable = true)
	private String imgUrl;
	
	
	/** Pet's size. */
	@Column(name = "pet_size", nullable = true)
    private String size;
	
	
	/** Pet's listing date. */
	@Column(name = "created_at", nullable = false)
	private LocalDate createdAt;


	/** Default constructor required by JPA. */
	public Pet() {
	}
	
	
	/**
     * Constructs a fully initialized Pet entity.
     *
     * @param name pet's listed name
     * @param breed pet's listed breed
     * @param age pet's listed age
     * @param gender pet's listed gender
     * @param pet_type is the pet a dog or cat
	 * @param location website pet is on
	 * @param price pet's listed price
     * @param pet_size pet's size
     * @param pet_status pet's adoption status
	 * @param imgUrl pet's accompanying image/s
	 * @param size pet's listed size
	 * @param createdAt pet's time of listing
     */
	public Pet(String name, String breed, int age, char gender, 
		String pet_type, String location, double price, 
		String petSize, String petStatus,
		String imgUrl, LocalDate createdAt
	) {
		this.name = name;
		this.breed = breed;
		this.age = age;
		this.gender = gender;
		this.petType = pet_type;
		this.location = location;
		this.price = price;
		this.size = petSize;
		this.petStatus = petStatus;
		this.imgUrl = imgUrl;
		this.createdAt = createdAt;
	}
 }
