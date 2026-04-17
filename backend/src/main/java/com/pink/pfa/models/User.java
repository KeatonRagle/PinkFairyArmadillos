package com.pink.pfa.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;


/**
 * JPA entity representing a user record in the database.
 * <p>
 * This class maps directly to the underlying User table and defines
 * the schema used for persistence. It is used by Spring Data JPA
 * through {@link jakarta.persistence.Entity}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Store user identity information (name, email).</li>
 *   <li>Store authentication credentials (hashed password).</li>
 *   <li>Define authorization role (USER or ADMIN).</li>
 *   <li>Persist optional metadata such as location.</li>
 * </ul>
 *
 * Security Notes:
 * <ul>
 *   <li>The password field should always store a hashed value (never plain text).</li>
 *   <li>The role field controls authorization and is stored as a String
 *       via {@link jakarta.persistence.EnumType#STRING} for readability
 *       and safety against enum reordering.</li>
 * </ul>
 */
@Data
@Entity
public class User {


    /** Primary key identifier for the user (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
	
	
    /** User's display name (required). */
    @Column(name = "name", nullable = false)
    private String name;


    /** Unique email used as the login identifier (required). */
    @Column(name = "email", nullable = false)
    private String email;


    /** Hashed password used for authentication (required). */
    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPref> preferences = new ArrayList<>();

    /** Authorization role determining user permissions. */
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER; // this is the default can be changed by an admin in the admin login?
	
	
	/** User's banned status.  */
	@Column(name = "is_banned", nullable = false)
	private boolean isBanned = false;
	
	
	/** User's contributor status check. */
	@Column(name = "requested_contributor", nullable = false)
	private boolean requestedContributor = false;
	
	
    /** Optional location metadata associated with the user. */
    private String location;


    /**
     * Enumeration of supported user roles.
     * USER represents a standard authenticated user.
     * ADMIN represents a privileged user with elevated permissions.
     */
    public enum Role { ROLE_USER, ROLE_CONTRIBUTOR, ROLE_ADMIN }

    /** Default constructor required by JPA. */
    public User() {
    }


    /**
     * Constructs a fully initialized User entity.
     *
     * @param name user's display name
     * @param email unique login email
     * @param password hashed password
     * @param role authorization role
     * @param location optional location metadata
	 * @param isBanned profile ban status
	 * @param requestedContributor user requested contributor status
     */
    public User(String name, String email, String password, Role role,
		String location, boolean isBanned, boolean requestedContributor
	) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.location = location;
		this.isBanned = isBanned;
		this.requestedContributor = requestedContributor;
    }


    /*+++ Getters +++*/
    public Integer getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;            
    }
	
	public boolean getIsBanned() {
		return isBanned;
	}
	
	public boolean getRequestedContributor() {
		return requestedContributor;
	}


    /*+++ Setters +++*/
    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(Role role) {
        this.role = role;
    }
	
	public void setIsBanned(boolean isBanned) {
		this.isBanned = isBanned;
	}
	
	public void setRequestedContributor(boolean requestedContributor) {
		this.requestedContributor = requestedContributor;
	}
}
