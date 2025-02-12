package com.lfey.lfenuserservice.service.auth;

import com.lfey.lfenuserservice.dto.UserAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public String createToken(UserAuth userLog) throws BadCredentialsException{
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLog.getEmail(), userLog.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid login or password");
        }
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(userLog.getEmail());
        return jwtUtils.generateToken(userDetails);
    }
}
