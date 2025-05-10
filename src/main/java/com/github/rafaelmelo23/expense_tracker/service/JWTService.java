package com.github.rafaelmelo23.expense_tracker.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Date;

/**
 * Service responsible for handling JSON Web Token (JWT) operations,
 * such as generation, periodic rotation for Prometheus scraping,
 * verification, and extraction of claims.
 */
@Service
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

    @Value("${jwt.algorithm.key}")
    private String algorithmKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiry.in.seconds}")
    private int expiryInSeconds;

    private static final String EMAIL_KEY = "EMAIL";
    private static final String ROLE_KEY = "ROLE";

    private Algorithm algorithm;

    @Value("${jwt.prometheus.user.email}")
    private String prometheusEmail;

    @Value("${jwt.prometheus.token.filepath}")
    private String prometheusTokenFilePath;

    /**
     * Initializes the Algorithm object using the provided algorithm key.
     */
    @PostConstruct
    public void postConstruct() {
        try {
            algorithm = Algorithm.HMAC256(algorithmKey);
            logger.info("Initialized JWT algorithm for HMAC256");
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to initialize JWT algorithm", e);
            throw new RuntimeException("Unable to configure JWT algorithm", e);
        }
    }

    /**
     * Generates a JWT for the given user. The token includes the user's
     * email and role as claims, an expiration time, and the issuer.
     */
    public String generateJWT(LocalUser user) {
        String token = JWT.create()
                .withClaim(EMAIL_KEY, user.getEmail())
                .withClaim(ROLE_KEY, user.getRole().name())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiryInSeconds * 1000L))
                .withIssuer(issuer)
                .sign(algorithm);
        logger.debug("Generated JWT for user {} with role {} expires in {} seconds",
                user.getEmail(), user.getRole(), expiryInSeconds);
        return token;
    }

    /**
     * Checks if the provided JWT has expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            Date expiresAt = decodedJWT.getExpiresAt();
            boolean expired = expiresAt.before(new Date());
            logger.debug("Token expires at {} - expired={}", expiresAt, expired);
            return expired;
        } catch (Exception e) {
            logger.warn("Token verification failed or token is invalid/expired", e);
            return true;
        }
    }

    /**
     * Extracts the email address from the given JWT.
     */
    public String getEmailFromToken(String token) {
        DecodedJWT jwt = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
        return jwt.getClaim(EMAIL_KEY).asString();
    }

    /**
     * Extracts the user's role from the given JWT.
     */
    public String getRoleFromToken(String token) {
        DecodedJWT jwt = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
        return jwt.getClaim(ROLE_KEY).asString();
    }

    /**
     * Scheduled method that generates an admin token for Prometheus,
     * allowing it to scrape metrics but remain role-protected.
     * It runs after startup and then every week thereafter.
     */
    @Scheduled(
            initialDelayString = "${jwt.prometheus.rotation.initial-delay-millis}",
            fixedDelayString   = "${jwt.prometheus.rotation.fixed-delay-millis}")
    public void rotatePrometheusToken() {
        logger.info("Starting Prometheus token rotation");
        LocalUser prometheusUser = new LocalUser();
        prometheusUser.setEmail(prometheusEmail);
        prometheusUser.setRole(Role.ROLE_ADMIN);

        String token = generateJWT(prometheusUser);

        try {
            Path path = Paths.get(prometheusTokenFilePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, token, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-r--r--"));
            logger.info("Wrote new Prometheus token to {}", prometheusTokenFilePath);
        } catch (IOException e) {
            logger.error("Failed to rotate Prometheus token", e);
            throw new RuntimeException("Could not write Prometheus token to disk", e);
        }
    }
}
