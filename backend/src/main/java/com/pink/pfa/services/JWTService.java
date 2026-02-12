package com.pink.pfa.services;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;



@Service
public class JWTService {

    private String key;

    /**
     * 
     * @return SecretKey
     * */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * 
     * @param token
     * @return {@link Claims}
     * */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload(); 
    }


    /**
     * 
     * @param <T>
     * @param token
     * @param claimResolver
     * @return Claims::claimResolver
     * */
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }


    /**
     * 
     * @param token
     * @return {@link Date}
     * */
    private Date extractExperation(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    

    /**
     * 
     * @param token
     * @return boolean
     * */
    private boolean isTokenExpired(String token) {
        return extractExperation(token).before(new Date());
    }


    /**
     *
     * Constructor 
     * */
    public JWTService () {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            key = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }


    /**
     * 
     * @param email
     * @return {@link Jwts}
     * */
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() * 60 * 60 * 30))
                .and()
                .signWith(getKey())
                .compact();
    }


    /**
     * 
     * @param token
     * @param userDetails
     * @return boolean
     * */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 
     * @param token
     * @return {@link String}
     * */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }


}
