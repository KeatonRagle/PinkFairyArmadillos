package com.pink.pfa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.pink.pfa.controllers.requests.CreateUserRequest;
import com.pink.pfa.services.UserService;

@TestConfiguration
public class TestDataConfig {

    @Bean
    CommandLineRunner seedTestUsers(UserService userService) {
        return args -> {
            try {
                userService.createUser(new CreateUserRequest(
                    "Austin", "austin@pfa.com", "foobar1"
                ));

                userService.createUser(new CreateUserRequest(
                    "Dylan", "Dylan@pfa.com", "foobar12"
                ));

                userService.createUser(new CreateUserRequest(
                    "Keaton", "Keaton@pfa.com", "foobar13"
                ));
            } catch (Exception ignored) {
                // if duplicate, ignore
            }
        };
    }
}
