package com.groupa.digitalbackendapplication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TokenService {

    @Value("${jwt.secret.string}")
    private String JWT_SECRET;

    @Value("${jwt.expiration.time}")
    private long EXPIRATION_TIME;

    @Value("${jwt.refresh.expiration.time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey())
                .compact();
    }

    public String generateRefreshToken(String email){
        Map<String, Object> claims = new HashMap<>();
        claims.put("refresh", "A refresh token");
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .claims(claims)
                .signWith(getSignKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean containsRefreshToken(String token) {
        return extractAllClaims(token).get("refresh") != null;
    }

    private Boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean validateRefreshToken(String token) {
        System.out.println(extractAllClaims(token).get("refresh") != null);
        System.out.println(isTokenExpired(token));
        if ( extractAllClaims(token).get("refresh") != null && !isTokenExpired(token) ) {
            return true;
        }
        return false;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
