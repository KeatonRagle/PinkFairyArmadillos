package com.pink.pfa.endpoints;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.db_integration.UserDTO;
import com.pink.pfa.db_integration.UserService;

@RestController
@RequestMapping("api/users")
public class UserController {
    // Singleton object the controller uses to interface with the database
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Map<String, Object> getAllUsers() {
        return Map.of(
            "Users: ", userService.findAll(),
            "TimeStamp", Instant.now().toString()
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(
        @PathVariable Long id
    ) {
        return Map.of(
            "ID: ", userService.findById(id),
            "TimeStamp", Instant.now().toString()
        );
    }

    @PostMapping()
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
}
