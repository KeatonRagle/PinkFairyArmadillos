package com.pink.pfa.services;

import java.util.List;

import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.models.User;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.repos.UserRepository;

@Service
@RequestMapping("/api/users")
public class UserService {
    // The singleton backend repository that takes our input and turns it into CRUD (abstracting out our data access layer)
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Grabs a list into a stream, maps the information in the stream to a map of 
    // Data Transfer Objects (allows you to censor important information), before packaging back into a list
    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserDTO::fromEntity)
                .toList();
    }

    // Finds a customer by id and converts to a DTO using the fromEntity lambda or throws an exception if it cannot be found
    public UserDTO findById(Integer id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new InvalidConfigurationPropertyValueException("Failed to Find ID", null, "User not found"));
    }

    // Processes creating a new Customer object, saving it to the database before returning the censored contents
    public UserDTO createUser(UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(
            passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }
}
