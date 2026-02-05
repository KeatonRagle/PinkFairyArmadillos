package com.pink.pfa.db_integration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

// The Java representation of an element of our User schema
@Data
@Entity
public class User {
    @Id
    private Integer user_id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "role", nullable = false)
    private String role;

    private String location;

    // Constructors
    public User() {
    }

    public User(String name, String email, String password, String role, String location) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.location = location;
    }
}