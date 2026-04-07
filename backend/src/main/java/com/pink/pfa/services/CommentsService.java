package com.pink.pfa.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pink.pfa.controllers.requests.CommentRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.Comments;
import com.pink.pfa.models.datatransfer.CommentDTO;
import com.pink.pfa.repos.CommentsRepository;
import com.pink.pfa.repos.UserRepository;

@Service
public class CommentsService {
    @Autowired private CommentsRepository commentsRepository;
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
    
    public Boolean existsById(Integer id) { 
        return commentsRepository.existsById(id);
    }

    public void deletePost(Integer id) { 
        commentsRepository.deleteById(id); 
    }

    public CommentDTO submitNewComment(CommentRequest request) {
        Comments comment = new Comments();
        comment.setCtDate(LocalDate.now());
        comment.setCtComment(request.comment());
        comment.setUser(userRepository.findById(request.userID())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.userID()))
        );

        Comments savedComment = commentsRepository.save(comment);

        return CommentDTO.fromEntity(savedComment);
    }
}
