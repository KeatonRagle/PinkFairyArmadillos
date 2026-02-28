package com.pink.pfa.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


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
 public class Pet {
	
	
	/** Primary key identifier for the pet (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pet_id;
	
	
	/** Foreign key identifier for the adoptionSite. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id", nullable = false)
	private AdoptionSite site;
	
	
	/** Pet's display name (required). */
    @Column(name = "name", nullable = false)
    private String name;
	
	
	/** Pet's breed. */
	@Column(name = "breed", nullable = false)
	private String breed;
	
	
	/** Pet's age. */
	@Column(name = "age", nullable = false)
	private int age;
	
	
	/** Pet's gender. */
	@Column(name = "gender", nullable = false)
	private char gender;
	
	
	/** Type of pet. */
	@Column(name = "type", nullable = false)
	private String pet_type;
	
	
	/** Location of pet. */
	@Column(name = "type", nullable = false)
	private String location;
	
	
	/** Pet's price. */
	@Column(name = "price", nullable = false)
	private double price;
	
	
	/** Pet's status. */
	@Column(name = "status", nullable = false)
	private String pet_status;
	
	
	/** Pet's compatability score with User. */
	@Column(name = "compatability_score", nullable = true)
	private int compatability_score;
	
	
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
     * @param pet_status pet's adoption status
     * @param compatability_score pet's listed compatability score
     */
	public Pet(String name, String breed, int age, char gender, String pet_type, String location, double price, String pet_status, int compatability_score) {
		this.name = name;
		this.breed = breed;
		this.age = age;
		this.gender = gender;
		this.pet_type = pet_type;
		this.location = location;
		this.price = price;
		this.pet_status = pet_status;
		this.compatability_score = compatability_score;
	}
	
	
	/*+++ Getters +++*/
	public Integer getPet_id() {
		return pet_id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getBreed() {
		return breed;
	}
	
	public int getAge() {
		return age;
	}
	
	public char getGender() {
		return gender;
	}
	
	public String getPet_type() {
		return pet_type;
	}
	
	public String getLocation() {
		return location;
	}
	
	public double getPrice() {
		return price;
	}
	
	public String getPet_status() {
		return pet_status;
	}
	
	public int getCompatability_score() {
		return compatability_score;
	}
	
	
	/*+++ Setters +++*/
	public void setName(String name) {
		this.name = name;
	}
	
	public void setBreed(String breed) {
		this.breed = breed;
	}
	
	public void setAge(int age) {
		this.age = age;
	}
	
	public void setGender(char gender) {
		this.gender = gender;
	}
	
	public void setPet_type(String pet_type) {
		this.pet_type = pet_type;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setPet_status(String pet_status) {
		this.pet_status = pet_status;
	}
	
	public void setCompatability_score(int compatability_score) {
		this.compatability_score = compatability_score;
	}
 }