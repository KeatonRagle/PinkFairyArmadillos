package com.pink.pfa.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pink.pfa.controllers.requests.CommentRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.Comments;
import com.pink.pfa.models.datatransfer.CommentDTO;
import com.pink.pfa.repos.CommentsRepository;
import com.pink.pfa.repos.PostsRepository;
import com.pink.pfa.repos.UserRepository;

@Service
public class CommentsService {
    @Autowired private CommentsRepository commentsRepository;
    @Autowired private PostsRepository postsRepository;
    @Autowired private UserRepository userRepository;

    public List<CommentDTO> findAll() { 
        return commentsRepository.findAll()
        .stream()
        .map(CommentDTO::fromEntity)
        .toList(); 
    } 

    public CommentDTO findById(Integer id) { 
        return commentsRepository.findById(id)
            .map(CommentDTO::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }

    public List<CommentDTO> findByPostId(Integer postId) {
        return commentsRepository.findByPost_PostId(postId)
        .stream()
        .map(CommentDTO::fromEntity)
        .toList(); 
    }
    
    public Boolean existsById(Integer id) { 
        return commentsRepository.existsById(id);
    }

    public void deleteComment(Integer id) { 
        commentsRepository.deleteById(id); 
    }

    public CommentDTO submitNewComment(CommentRequest request) {
        Comments comment = new Comments(LocalDateTime.now(), request.comment());
        comment.setUser(userRepository.findById(request.userID())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.userID()))
        );
        comment.setPost(postsRepository.findById(request.postID())
            .orElseThrow(() -> new ResourceNotFoundException("Post", request.postID()))
        );

        Comments savedComment = commentsRepository.save(comment);
        return CommentDTO.fromEntity(savedComment);
    }
}
