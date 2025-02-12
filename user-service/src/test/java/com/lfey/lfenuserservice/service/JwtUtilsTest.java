package com.lfey.lfenuserservice.service;

import com.lfey.lfenuserservice.entity.Role;
import com.lfey.lfenuserservice.entity.User;
import com.lfey.lfenuserservice.service.auth.JwtUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;


@SpringBootTest
class JwtUtilsTest {
    private static final Logger log = LoggerFactory.getLogger(JwtUtilsTest.class);

    @Autowired
    private JwtUtils jwtUtils;


}