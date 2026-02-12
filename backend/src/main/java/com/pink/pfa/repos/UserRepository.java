package com.pink.pfa.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pink.pfa.models.User;

// Simple and plain extension on the JpaRepository interface

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByName(String name);
    User findByEmail(String email);
    //User findById(Integer user_id);
}
