package com.pink.pfa.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.exceptions.UserAlreadyExistsException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.User;
import com.pink.pfa.repos.AdoptionSiteRepository;
import com.pink.pfa.repos.PetRepository;
import com.pink.pfa.repos.UserRepository;
import com.pink.pfa.services.UserService;

@TestConfiguration
public class TestDataConfig {

    @Bean
    CommandLineRunner seedTestUsers(UserService userService, UserRepository userRepository) {
        return args -> {
            UserRequest[] users = {
                new UserRequest("Austin",   "austin@pfa.com",   "foobar01"),
                new UserRequest("Dylan",    "dylan@pfa.com",    "foobar02"),
                new UserRequest("Keaton",   "keaton@pfa.com",   "foobar03"),
                new UserRequest("Jordan",   "jordan@pfa.com",   "foobar04"),
                new UserRequest("Taylor",   "taylor@pfa.com",   "foobar05"),
                new UserRequest("Morgan",   "morgan@pfa.com",   "foobar06"),
                new UserRequest("Casey",    "casey@pfa.com",    "foobar07"),
                new UserRequest("Riley",    "riley@pfa.com",    "foobar08"),
                new UserRequest("Avery",    "avery@pfa.com",    "foobar09"),
                new UserRequest("Parker",   "parker@pfa.com",   "foobar10"),
                new UserRequest("Quinn",    "quinn@pfa.com",    "foobar11"),
                new UserRequest("Sage",     "sage@pfa.com",     "foobar12"),
                new UserRequest("Drew",     "drew@pfa.com",     "foobar13"),
                new UserRequest("Blake",    "blake@pfa.com",    "foobar14"),
                new UserRequest("Jamie",    "jamie@pfa.com",    "foobar15"),
                new UserRequest("Reese",    "reese@pfa.com",    "foobar16"),
                new UserRequest("Skyler",   "skyler@pfa.com",   "foobar17"),
                new UserRequest("Cameron",  "cameron@pfa.com",  "foobar18"),
                new UserRequest("Alex",     "alex@pfa.com",     "foobar19"),
                new UserRequest("Robin",    "robin@pfa.com",    "foobar20")
            };

            for (UserRequest user : users) {
                try {
                    userService.createUser(user);
                } catch (UserAlreadyExistsException ignored) {
                    // skip duplicates on restart
                }
            }

            String[] admins = {
                "austin@pfa.com",
                "dylan@pfa.com",
                "keaton@pfa.com",
                "jordan@pfa.com",
                "taylor@pfa.com"
            };

            String[] contributors = {
                "morgan@pfa.com",
                "casey@pfa.com",
                "riley@pfa.com",
                "avery@pfa.com",
                "parker@pfa.com"
            };

            for (String email : admins) {
                try {
                    User user = userRepository.findByEmail(email).orElseThrow();
                    userService.promoteToAdmin(user.getUserId());
                } catch (Exception ignored) {}
            }

            for (String email : contributors) {
                try {
                    User user = userRepository.findByEmail(email).orElseThrow();
                    userService.promoteToContributor(user.getUserId());
                } catch (Exception ignored) {}
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
                
                Pet dog = new Pet("Buddy", "Labrador Retriever", 24, 'M', "Dog", "Austin, TX", 150.0, "Medium", "available", 85);
                dog.setSite(site);
                petRepo.save(dog);

                Pet dog2 = new Pet("Rex", "Golden Retriever", 104, 'F', "Dog", "Austin, TX", 150.0, "Large", "pending", 85);
                dog2.setSite(site);
                petRepo.save(dog2);

                Pet dog3 = new Pet("Pal", "German Shephard", 50, 'M', "Dog", "Austin, TX", 150.0, "Large", "available", 85);
                dog3.setSite(site);
                petRepo.save(dog3);

                Pet cat = new Pet("Luna", "Domestic Shorthair", 12, 'F', "Cat", "Austin, TX", 75.0, "Small", "available", 90);
                cat.setSite(site);
                petRepo.save(cat);

                Pet cat2 = new Pet("Sol", "Domestic Shorthair", 36, 'M', "Cat", "Austin, TX", 75.0, "Medium", "available", 90);
                cat2.setSite(site);
                petRepo.save(cat2);
            } catch (Exception ignored) {
                // if duplicate or FK issue, ignore
            }
        };
    }
}
