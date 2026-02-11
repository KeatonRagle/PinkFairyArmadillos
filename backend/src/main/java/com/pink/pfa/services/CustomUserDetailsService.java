package com.pink.pfa.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pink.pfa.models.User;
import com.pink.pfa.models.details.UserPrincipal;
import com.pink.pfa.repos.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = email.trim().toLowerCase();
        User user = repo.findByEmail(normalized);

        if (user == null) {
            System.out.println("User not found" + email);
            throw new UsernameNotFoundException("User not found: " + email);
        }
        return new UserPrincipal(user);  

    }
}
