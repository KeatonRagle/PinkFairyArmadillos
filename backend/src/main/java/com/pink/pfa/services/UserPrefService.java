package com.pink.pfa.services;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pink.pfa.controllers.requests.UserPrefRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.User;
import com.pink.pfa.models.UserPref;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.repos.UserPrefRepository;
import com.pink.pfa.repos.UserRepository;

@Service
public class UserPrefService {
    private final UserPrefRepository userPrefRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public UserPrefService(
        UserPrefRepository userPrefRepository,
        UserRepository userRepository,
        UserService userService
    ) {
        this.userPrefRepository = userPrefRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public UserPref findById(Integer id) { 
        return userPrefRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("UserPref", id));
    }

    public List<UserPref> findAllByUserId(Integer userId) { 
        return userPrefRepository.findByUser_UserId(userId)
            .stream()
            .toList();
    }

    public List<UserPref> findAllByEmail(String email) { 
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return findAllByUserId(user.getUserId());
    }

    public void deleteUserPref(Integer prefId) { 
        UserDTO user = userService.findByJWT();
        UserPref pref = userPrefRepository.findByIdAndUser_UserId(prefId, user.id())
            .orElseThrow(() -> new RuntimeException("Preference not found or not yours"));

        userPrefRepository.delete(pref);
    }

    public UserPref createNewPref(UserPrefRequest prefRequest) {
        UserDTO user = userService.findByJWT();
        UserPref userPref = new UserPref(prefRequest.pref(), prefRequest.value());
        userPref.setUser(userRepository.findById(user.id())
            .orElseThrow(() -> new ResourceNotFoundException("User", user.id()))
        );

        UserPref savedPref = userPrefRepository.save(userPref);
        return savedPref;
    }
}
