package com.lfey.gatewayservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;


@Component
public class JwtUtils {

    @Value("${secret.key}")
    private String key;

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new JwtValidateException("Token expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtValidateException("Invalid token", ex);
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserEmailFromToken(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public String getUserIdFromToken(String token) {
        Long id =  extractAllClaims(token).get("id", Long.class);
        return id.toString();
    }

    public String getUserRoleFromToken(String token) {
        return extractAllClaims(token).get("roles", String.class);
    }
}
