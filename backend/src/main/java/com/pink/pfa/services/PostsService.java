package com.pink.pfa.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pink.pfa.controllers.requests.PostRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.Posts;
import com.pink.pfa.models.datatransfer.PostDTO;
import com.pink.pfa.repos.PostsRepository;
import com.pink.pfa.repos.UserRepository;

@Service
public class PostsService {
    @Autowired private PostsRepository postsRepository;
    @Autowired private UserRepository userRepository;

    public List<PostDTO> findAll() { 
        return postsRepository.findAll()
        .stream()
        .map(PostDTO::fromEntity)
        .toList(); 
    } 

    public PostDTO findById(Integer id) { 
        return postsRepository.findById(id)
            .map(PostDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }
    
    public Boolean existsById(Integer id) { 
        return postsRepository.existsById(id);
    }

    public void deletePost(Integer id) { 
        postsRepository.deleteById(id); 
    }

    public PostDTO submitNewPost(PostRequest request) {
        Posts post = new Posts();
        post.setPostDate(LocalDateTime.now());
        post.setPostContent(request.comment());
        post.setUser(userRepository.findById(request.userID())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.userID()))
        );

        Posts savedPost = postsRepository.save(post);
        return PostDTO.fromEntity(savedPost);
    }
}
