package com.pink.pfa.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.repos.AdoptionSiteRepository;
import com.pink.pfa.repos.PetRepository;
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

    @Bean
    CommandLineRunner seedTestPets(AdoptionSiteRepository siteRepo, PetRepository petRepo) {
        return args -> {
            try {
                AdoptionSite site = new AdoptionSite(
                    "Happy Paws Shelter",
                    "contact@happypaws.org",
                    4.5,
                    "Austin, TX"
                );
                site = siteRepo.save(site);

                Pet dog = new Pet("Buddy", "Labrador Retriever", 3, 'M', "dog", "Austin, TX", 150.0, "available", 85);
                dog.setSite(site);
                petRepo.save(dog);

                Pet cat = new Pet("Luna", "Domestic Shorthair", 2, 'F', "cat", "Austin, TX", 75.0, "available", 90);
                cat.setSite(site);
                petRepo.save(cat);

                Pet dog2 = new Pet("Max", "German Shepherd", 5, 'M', "dog", "Austin, TX", 200.0, "pending", 70);
                dog2.setSite(site);
                petRepo.save(dog2);
            } catch (Exception ignored) {
                // if duplicate or FK issue, ignore
            }
        };
    }
}
