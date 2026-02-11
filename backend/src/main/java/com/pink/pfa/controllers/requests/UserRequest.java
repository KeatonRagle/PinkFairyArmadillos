package com.pink.pfa.controllers.requests;

// Immutable class for representing a customer table request; all components are private 
public record UserRequest(
        String name, 
        String email,
        String password
) {}
