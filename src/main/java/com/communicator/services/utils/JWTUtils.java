package com.communicator.services.utils;

import com.communicator.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.SignatureAlgorithm;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for handling JWT token operations such as generation, validation, and claims
 * extraction.
 * It uses a secret key for signing and verifying tokens.
 */
@Component
public class JWTUtils {

    private SecretKey secretKey;

    @Value("${application.security.jwt.secret-string}")
    private String secretString;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Initializes the secret key for signing JWT tokens.
     */
    @SuppressWarnings("PMD")
    @PostConstruct
    private void init() {
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims in the token
     */
    private Claims extractAllClaims(final String token) {
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts a specific claim from a token using a resolver function.
     *
     * @param <T>      the type of the claim to extract
     * @param token    the JWT token
     * @param resolver the function to resolve the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(final String token, final Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Extracts the phone number (subject) from the token.
     *
     * @param token the JWT token
     * @return the phone number
     */
    public String extractPhoneNumber(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generates a JWT token for the given user.
     *
     * @param appUser       the user for whom the token is generated
     * @param expiryTime the expiration time of the token in milliseconds
     * @return the generated JWT token
     */
    public String generateToken(final AppUser appUser, final long expiryTime) {
        return Jwts.builder()
                .subject(appUser.getPhoneNumber())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiryTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates an access token for the user using the default expiration time.
     *
     * @param appUser the user for whom the token is generated
     * @return the generated access token
     */
    public String generateAccessToken(final AppUser appUser) {
        return generateToken(appUser, accessTokenExpiration);
    }

    /**
     * Checks if a token is valid for the given user.
     *
     * @param token the JWT token
     * @param appUser  the user to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(final String token, final AppUser appUser) {
        final String phoneNumber = extractPhoneNumber(token);
        return phoneNumber.equals(appUser.getPhoneNumber()) && !isTokenExpired(token);
    }

    /**
     * Checks if the token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
