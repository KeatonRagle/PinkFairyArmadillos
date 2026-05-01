package com.pink.pfa.services;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pink.pfa.controllers.requests.UserPrefRequest;
import com.pink.pfa.exceptions.ResourceNotFoundException;
import com.pink.pfa.models.User;
import com.pink.pfa.models.UserPreferences;
import com.pink.pfa.models.UserPreferences.Preference;
import com.pink.pfa.models.datatransfer.UserDTO;
import com.pink.pfa.repos.UserPreferencesRepository;
import com.pink.pfa.repos.UserRepository;

@Service
public class UserPrefService {
    private final UserPreferencesRepository userPrefRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public UserPrefService(
        UserPreferencesRepository userPrefRepository,
        UserRepository userRepository,
        UserService userService
    ) {
        this.userPrefRepository = userPrefRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public UserPreferences findById(Integer id) { 
        return userPrefRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("UserPreferences", id));
    }

    public List<UserPreferences> findAllByUserId(Integer userId) { 
        return userPrefRepository.findByUser_UserId(userId)
            .stream()
            .toList();
    }

    public List<UserPreferences> findAllByEmail(String email) { 
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return findAllByUserId(user.getUserId());
    }

    public void deleteUserPref(Integer prefId) { 
        UserDTO user = userService.findByJWT();
        UserPreferences pref = userPrefRepository.findByUser_UserIdAndPrefId(user.id(), prefId)
            .orElseThrow(() -> new RuntimeException("Preference not found or not yours"));

        userPrefRepository.delete(pref);
    }

    public UserPreferences createNewPref(UserPrefRequest prefRequest) {
        UserDTO user = userService.findByJWT();
        UserPreferences UserPreferences = new UserPreferences(Preference.valueOf(prefRequest.pref()), prefRequest.value());
        UserPreferences.setUser(userRepository.findById(user.id())
            .orElseThrow(() -> new ResourceNotFoundException("User", user.id()))
        );

        UserPreferences savedPref = userPrefRepository.save(UserPreferences);
        return savedPref;
    }
}
