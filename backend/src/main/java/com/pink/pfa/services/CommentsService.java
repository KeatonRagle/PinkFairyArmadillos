package com.pink.pfa.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.Comments;
import com.pink.pfa.models.datatransfer.CommentDTO;
import com.pink.pfa.repos.CommentsRepository;

@Service
public class CommentsService {
    @Autowired private CommentsRepository commentsRepository;

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

    public Comments createPost(Comments comment) { 
        return commentsRepository.save(comment); 
    }
}
