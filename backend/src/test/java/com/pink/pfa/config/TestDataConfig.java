package com.pink.pfa.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.exceptions.UserAlreadyExistsException;
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
            UserRequest[] users = {
                new UserRequest("Austin", "austin@pfa.com", "foobar1"),
                new UserRequest("Dylan", "dylan@pfa.com", "foobar12"),
                new UserRequest("Keaton", "keaton@pfa.com", "foobar13"),
                new UserRequest("Jordan", "jordan@pfa.com", "foobar14"),
                new UserRequest("Taylor", "taylor@pfa.com", "foobar15"),
                new UserRequest("Morgan", "morgan@pfa.com", "foobar16"),
                new UserRequest("Casey", "casey@pfa.com", "foobar17"),
                new UserRequest("Riley", "riley@pfa.com", "foobar18")
            };

            for (UserRequest user : users) {
                try {
                    userService.createUser(user);
                } catch (UserAlreadyExistsException ignored) {
                    // skip duplicates on restart
                }
            }
        };
    }

    @Bean
    CommandLineRunner seedTestPets(AdoptionSiteRepository siteRepo, PetRepository petRepo) {
        return args -> {
            try {
                AdoptionSite site = new AdoptionSite(
                    "Dallas County",
                    "",
                    "",
                    0,
                    "https://hsdallascounty.org"
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
