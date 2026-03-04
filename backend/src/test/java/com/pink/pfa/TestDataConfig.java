package com.pink.pfa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.services.UserService;

@TestConfiguration
public class TestDataConfig {

    @Bean
    CommandLineRunner seedTestUsers(UserService userService) {
        return args -> {
            try {
                userService.createUser(new UserRequest(
                    "Austin", "austin@pfa.com", "foobar1"
                ));

                userService.createUser(new UserRequest(
                    "Dylan", "Dylan@pfa.com", "foobar12"
                ));

                userService.createUser(new UserRequest(
                    "Keaton", "Keaton@pfa.com", "foobar13"
                ));
            } catch (Exception ignored) {
                // if duplicate, ignore
            }
        };
    }
}
