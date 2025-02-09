package com.lfey.lfenuserservice.service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JwtUtils {

    @Value("${token.signing.key}")
    private String key;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        String roleList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findAny().get();
        claims.put("roles", roleList);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, Decoders.BASE64.decode(key))
                .compact();
    }
}
