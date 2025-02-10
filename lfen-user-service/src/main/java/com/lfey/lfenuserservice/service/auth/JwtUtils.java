package com.lfey.lfenuserservice.service.auth;

import com.lfey.lfenuserservice.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${token.signing.key}")
    private String key;

    public String generateToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        String roleList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findAny().get();
        claims.put("roles", roleList);
        claims.put("username", userDetails.getRealUsername());
        claims.put("id", userDetails.getUserId());
        claims.put("email", userDetails.getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, Decoders.BASE64.decode(key))
                .compact();
    }

    public CustomUserDetails createGetCustomUserDetails(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name())));
    }
}
