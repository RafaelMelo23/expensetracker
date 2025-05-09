package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.dao.LocalUserDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(properties = {
        "jwt.algorithm.key=testkey1234567890",
        "jwt.issuer=test-issuer",
        "jwt.expiry.in.seconds=3600"
})
public class JWTServiceTest {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private LocalUserDAO localuserDAO;

    @Test
    void testGenerateAndValidateJWT() {
        // Given: user from H2 data.sql
        LocalUser user = localuserDAO.findById(1L)
                .orElseThrow(() -> new IllegalStateException("User with id 1 not found"));

        // When: generate token
        String token = jwtService.generateJWT(user);

        // Then: validations
        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenExpired(token)).isFalse();
        assertThat(jwtService.getEmailFromToken(token)).isEqualTo(user.getEmail());
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo(user.getRole().name());
    }

    @Test
    void testExpiredTokenReturnsTrue() throws UnsupportedEncodingException {
        // Given: create an expired token manually
        String expiredToken = createExpiredToken(
                "expired@example.com",
                "USER",
                "test-issuer",
                "testkey1234567890"
        );

        // Then
        assertThat(jwtService.isTokenExpired(expiredToken)).isTrue();
    }

    private String createExpiredToken(String email, String role, String issuer, String key) throws UnsupportedEncodingException {
        return com.auth0.jwt.JWT.create()
                .withClaim("EMAIL", email)
                .withClaim("ROLE", role)
                .withIssuer(issuer)
                .withExpiresAt(new java.util.Date(System.currentTimeMillis() - 10000))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(key));
    }
}
