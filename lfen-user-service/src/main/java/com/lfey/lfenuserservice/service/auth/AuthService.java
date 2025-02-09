package com.lfey.lfenuserservice.service.auth;

import com.lfey.lfenuserservice.dto.UserLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(CustomUserDetailsService customUserDetailsService, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    public String createToken(UserLog userLog) throws BadCredentialsException{
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLog.getEmail(), userLog.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid login or password");
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userLog.getEmail());
        return jwtUtils.generateToken(userDetails);
    }
}
