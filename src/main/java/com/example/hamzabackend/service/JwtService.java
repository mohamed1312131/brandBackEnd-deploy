package com.example.hamzabackend.service;


import com.example.hamzabackend.entity.Admin;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private final String jwtSecret = getEnvValue("JWT_SECRET");
    private final long jwtExpiration = Long.parseLong(getEnvValue("JWT_EXPIRATION"));

    private String getEnvValue(String key) {
        // Try dotenv first, then fall back to system environment variables
        String value = dotenv.get(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            throw new RuntimeException("Environment variable " + key + " is not set");
        }
        return value;
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Admin admin) {
        return Jwts.builder()
                .setSubject(admin.getEmail())
                .claim("adminId", admin.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, Admin admin) {
        final String email = extractEmail(token);
        return (email.equals(admin.getEmail()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
