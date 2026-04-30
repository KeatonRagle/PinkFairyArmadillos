package com.pink.pfa.config;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.pink.pfa.controllers.requests.CommentRequest;
import com.pink.pfa.controllers.requests.PostRequest;
import com.pink.pfa.controllers.requests.UserRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.exceptions.UserAlreadyExistsException;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Comments;
import com.pink.pfa.models.FeaturedPets;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.Posts;
import com.pink.pfa.models.User;
import com.pink.pfa.repos.AdoptionSiteRepository;
import com.pink.pfa.repos.CommentsRepository;
import com.pink.pfa.repos.FeaturedPetsRepository;
import com.pink.pfa.repos.PetRepository;
import com.pink.pfa.repos.PostsRepository;
import com.pink.pfa.repos.UserRepository;
import com.pink.pfa.services.CommentsService;
import com.pink.pfa.services.PostsService;
import com.pink.pfa.services.UserService;

@TestConfiguration
public class TestDataConfig {
    @Bean
    
    //This should be reverted to being more modular, each entity is too interconnected now though for it to work currently
    CommandLineRunner seedTestUsersAndAdoptionCentersAndPetsAndPostsAndComments(
        UserService userService, UserRepository userRepository, 
        AdoptionSiteRepository siteRepo,  PetRepository petRepo,
        PostsRepository postRepo, CommentsRepository commentRepo,
        PostsService postService, CommentsService commentsService,
        FeaturedPetsRepository fPetRepo
    ) {   
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

            AdoptionSite site = new AdoptionSite(
                "Dallas County",
                "",
                "",
                "https://hsdallascounty.org",
                'A',
                LocalDate.now()
            );

            List<String> listUsers = Arrays.asList(contributors);
            Collections.shuffle(listUsers);
            site.setUser(userRepository.findByEmail(listUsers.get(0)).orElseThrow());
            site = siteRepo.save(site);

            Pet dog = new Pet("Buddy", "Labrador Retriever", 24, 'M', "Dog", "Austin, TX", 150.0, "Medium", "available", "Placeholder", LocalDate.now());
            dog.setSite(site);
            petRepo.save(dog);

            Pet dog2 = new Pet("Rex", "Golden Retriever", 104, 'F', "Dog", "Austin, TX", 150.0, "Large", "pending", "Placeholder", LocalDate.now());
            dog2.setSite(site);
            petRepo.save(dog2);

            Pet dog3 = new Pet("Pal", "German Shephard", 50, 'M', "Dog", "Austin, TX", 150.0, "Large", "available", "Placeholder", LocalDate.now());
            dog3.setSite(site);
            petRepo.save(dog3);

            Pet cat = new Pet("Luna", "Domestic Shorthair", 12, 'F', "Cat", "Austin, TX", 75.0, "Small", "available", "Placeholder", LocalDate.now());
            cat.setSite(site);
            petRepo.save(cat);

            Pet cat2 = new Pet("Sol", "Domestic Shorthair", 36, 'M', "Cat", "Austin, TX", 75.0, "Medium", "available", "Placeholder", LocalDate.now());
            cat2.setSite(site);
            petRepo.save(cat2);

            FeaturedPets dogF = new FeaturedPets();
            dogF.setPet(dog);

            FeaturedPets catF = new FeaturedPets();
            catF.setPet(cat);

            try {
                List<Integer> postIds = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    Collections.shuffle(listUsers);
                    Integer userId = userRepository.findByEmail(listUsers.get(0))
                    .orElseThrow(() -> new RuntimeException("User not found for email: " + listUsers.get(0)))
                    .getUserId();

                    PostRequest postReq = new PostRequest(userId, "TestPost " + i);
                    Posts post = new Posts(LocalDateTime.now(), postReq.post());
                    post.setUser(userRepository.findById(postReq.userID())
                        .orElseThrow(() -> new ResourceNotFoundException("User", postReq.userID()))
                    );

                    var createdPost = postRepo.save(post);
                    postIds.add(createdPost.getPostId());
                }

                for (int j = 0; j < 3; j++) {
                    int randIndex = (int)(Math.random() * postIds.size());
                    Integer postId = postIds.get(randIndex);

                    Collections.shuffle(listUsers);
                    Integer userId = userRepository.findByEmail(listUsers.get(0))
                    .orElseThrow(() -> new RuntimeException("User not found for email: " + listUsers.get(0)))
                    .getUserId();

                    CommentRequest commentReq = new CommentRequest(userId, postId, "TestResponse " + j);
                    Comments comment = new Comments(LocalDateTime.now(), commentReq.comment());
                    comment.setUser(userRepository.findById(commentReq.userID())
                        .orElseThrow(() -> new ResourceNotFoundException("User", commentReq.userID()))
                    );

                    comment.setPost(postRepo.findById(commentReq.postID())
                        .orElseThrow(() -> new ResourceNotFoundException("Post", commentReq.postID()))
                    );

                    commentRepo.save(comment);
                }   
                
                petRepo.flush();
                siteRepo.flush();
                userRepository.flush();
                commentRepo.flush();

                System.out.println("Comments count: " + commentsService.findAll().size());
            } catch (Exception e) {
                e.printStackTrace(); // or log it
            }
        };
    }
}
