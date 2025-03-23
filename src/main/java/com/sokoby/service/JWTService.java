package com.sokoby.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {
    @Value("${jwt.algorithm.key}")
    private String algorithmKey;
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.expiry.duration}")
    private long expiryTime;

    private Algorithm algorithm;

    private final String EMAIL = "username";

    @PostConstruct
    private void initializeAlgorithm() {
        algorithm = Algorithm.HMAC256(algorithmKey);
    }

    public String generateToken(String email) {
        String sign = JWT.create()
                        .withClaim(EMAIL,email)
                        .withExpiresAt(new Date(System.currentTimeMillis() + expiryTime))
                        .withIssuer(issuer)
                        .sign(algorithm);
        System.out.println("Token Generated : " + sign);
        return sign;
    }

    public String getUserEmail(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
            return decodedJWT.getClaim(EMAIL).asString();
        } catch (JWTVerificationException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            // Handle invalid or expired token
            return false;
        }
    }
}
