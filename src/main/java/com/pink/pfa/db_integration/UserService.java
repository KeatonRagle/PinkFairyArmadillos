package com.pink.pfa.db_integration;

import java.util.List;

import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pink.pfa.endpoints.UserRequest;

@Service
@RequestMapping("/api/users")
public class UserService {
    // The singleton backend repository that takes our input and turns it into CRUD (abstracting out our data access layer)
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new InvalidConfigurationPropertyValueException("Failed to Find ID", null, "User not found"));
    }

    // Processes creating a new Customer object, saving it to the database before returning the censored contents
    public UserDTO createUser(UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(
            passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);

        return UserDTO.fromEntity(savedUser);
    }
}
