package com.db_integration;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

// The Java representation of an element of our User schema
@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    private String name;
    private String email;
    private String password;
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