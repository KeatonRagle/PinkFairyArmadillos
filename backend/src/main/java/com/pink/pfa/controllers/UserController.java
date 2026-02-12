package com.pink.pfa.controllers;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.services.UserService;

import io.jsonwebtoken.Jwts;

import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.models.User;

@RestController
@RequestMapping("/api/users")
public class UserController {
    // Singleton object the controller uses to interface with the database
    @Autowired
    private UserService userService;


    /**
     * 
     * @return {@link Map} of all users
     * */
    @GetMapping
    public Map<String, Object> getAllUsers() {
        return Map.of(
            "Users: ", userService.findAll(),
            "TimeStamp", Instant.now().toString()
        );
    }


    /**
     * 
     * @param id
     * @return {@link Map} of user assigned to id
     * */
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(
        @PathVariable Integer id
    ) {
        return Map.of(
            "ID: ", userService.findById(id),
            "TimeStamp", Instant.now().toString()
        );
    }


    /**
     * 
     * @param request
     * @return {@link ResponseEntity} <UserDTO>  
     * */
    @PostMapping("/register")
    // @RequestBody tells Spring to create a new customer object with the request body. 
    public ResponseEntity<UserDTO> CreateUser (
        @RequestBody UserRequest request
    ) {
        UserDTO createdUser = userService.createUser(request);
        // Acts as a regular Java object with the added flavor of having the entire HTTP response instead of default 200 (OK)
        // Also lets you use the Builder pattern to do what I did here
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }


    /**
     * 
     * @param user
     * @return {@link String} containing a {@link Jwts}
     * */
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.verify(user);
    }
}
