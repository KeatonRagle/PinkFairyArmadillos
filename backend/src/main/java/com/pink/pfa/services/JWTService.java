package com.pink.pfa.services;

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


/**
 * Service responsible for creating and validating JSON Web Tokens (JWTs) used for stateless authentication.
 * <p>
 * This implementation uses the JJWT library to:
 * <ul>
 *   <li>Generate signed JWTs containing the user's email as the token subject.</li>
 *   <li>Parse and extract claims (subject, expiration, etc.) from incoming tokens.</li>
 *   <li>Validate tokens by verifying the signature, matching the token subject to a user, and checking expiration.</li>
 * </ul>
 *
 * Signing key behavior:
 * <ul>
 *   <li>A symmetric HMAC SHA-256 signing key is generated at service startup using {@link KeyGenerator}.</li>
 *   <li>The raw key bytes are Base64-encoded and stored in memory.</li>
 *   <li>Because the key is generated on startup, tokens issued before an application restart will become invalid
 *       after a restart (since the signing key changes).</li>
 * </ul>
 *
 * Notes:
 * <ul>
 *   <li>This service is Spring-managed via {@link Service} and is typically used by authentication controllers
 *       and/or security filters.</li>
 *   <li>The token "subject" is the user's email, which is used as the unique identifier when validating.</li>
 * </ul>
 */
@Service
public class JWTService {

    private String key;


    /**
     * Builds the signing key used to sign and verify JWTs.
     * Decodes the Base64-encoded secret into raw bytes and converts it into an HMAC SHA key
     * compatible with the JJWT library.
     *
     * @return HMAC {@link SecretKey} used for signing and verification
     */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Parses a signed JWT and returns the full set of claims contained in the token payload.
     * This method verifies the token signature using the configured signing key.
     *
     * @param token JWT string to parse
     * @return {@link Claims} extracted from the token payload
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload(); 
    }


    /**
     * Generic helper to extract a specific claim from a token using a resolver function.
     * This is used to avoid repeating parsing logic for each claim type.
     *
     * @param token JWT string to parse
     * @param claimResolver function that selects a value from {@link Claims}
     * @param <T> the type of the extracted claim value
     * @return extracted claim value
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }


    /**
     * Extracts the expiration timestamp from a JWT.
     *
     * @param token JWT string to parse
     * @return {@link Date} representing when the token expires
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    

    /**
     * Checks whether a JWT is expired by comparing its expiration time to the current system time.
     *
     * @param token JWT string to check
     * @return {@code true} if expired; otherwise {@code false}
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    /**
     * Constructs the service and generates a new secret signing key for HMAC SHA-256.
     * The key is stored in memory as a Base64-encoded string and is used for signing/verifying tokens.
     *
     * @throws RuntimeException if the cryptographic key generator cannot be initialized
     */
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
     * Generates a new signed JWT for the given email.
     * <p>
     * The token includes:
     * <ul>
     *   <li>{@code subject}: the user's email</li>
     *   <li>{@code issuedAt}: time of token creation</li>
     *   <li>{@code expiration}: time the token becomes invalid</li>
     * </ul>
     *
     * @param email email to store as the token subject
     * @return compact JWT string
     */
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60  * 30))  // 30 minutes
                .and()
                .signWith(getKey())
                .compact();
    }


    /**
     * Validates a token by confirming:
     * <ul>
     *   <li>The token subject matches the authenticated user's username/email.</li>
     *   <li>The token is not expired.</li>
     * </ul>
     * Signature verification occurs when the token is parsed for claims.
     *
     * @param token JWT string to validate
     * @param userDetails Spring Security user details to validate against
     * @return {@code true} if valid; otherwise {@code false}
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    
    /**
     * Extracts the email (token subject) from a JWT.
     *
     * @param token JWT string to parse
     * @return email stored in the token subject
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    /**
     * Extracts the user's email from an Authorization header containing a Bearer token.
     *
     * <p>If the header starts with {@code "Bearer "}, the token portion is extracted
     * and parsed to retrieve the email (subject claim). If the header is invalid,
     * malformed, or token parsing fails, {@code null} is returned.</p>
     *
     * @param authHeader the value of the HTTP Authorization header
     * @return the extracted email if present and valid; otherwise {@code null}
     */
    public String extractEmailFromHeader(String authHeader) {
        String token = null;
        String email = null;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = extractEmail(token);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        return email;
    }
}
