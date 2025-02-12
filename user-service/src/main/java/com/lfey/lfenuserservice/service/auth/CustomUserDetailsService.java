package com.lfey.lfenuserservice.service.auth;

import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.repository.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found")
                );
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}