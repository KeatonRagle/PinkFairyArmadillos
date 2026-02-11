package com.pink.pfa.db_integration;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// Simple and plain extension on the JpaRepository interface
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
    java.util.Optional<User> findByEmail(String email);
    //User findById(long user_id);
}
