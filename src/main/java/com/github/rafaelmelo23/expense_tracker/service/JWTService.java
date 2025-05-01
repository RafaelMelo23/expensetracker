package com.github.rafaelmelo23.expense_tracker.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt.algorithm.key}")
    private String algorithmKey;
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.expiry.in.seconds}")
    private int expiryInSeconds;

    private static final String EMAIL_KEY = "EMAIL";
    private static final String ROLE_KEY = "ROLE";
    private Algorithm algorithm;

    @PostConstruct
    public void postConstruct() {
        try {
            algorithm = Algorithm.HMAC256(algorithmKey);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateJWT(LocalUser user) {
        return JWT.create()
                .withClaim(EMAIL_KEY, user.getEmail())
                .withClaim(ROLE_KEY, user.getRole().name())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiryInSeconds * 1000L))
                .withIssuer(issuer)
                .sign(algorithm);
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            Date expiresAt = decodedJWT.getExpiresAt();
            return expiresAt.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getEmailFromToken(String token) {
        DecodedJWT jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);

        return jwt.getClaim(EMAIL_KEY).asString();
    }

    public String getRoleFromToken(String token) {
        DecodedJWT jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
        return jwt.getClaim(ROLE_KEY).asString();
    }
}
