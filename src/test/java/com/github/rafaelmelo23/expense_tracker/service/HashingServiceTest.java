package com.github.rafaelmelo23.expense_tracker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link HashingService} that tests the service with actual
 * Spring configuration and property values.
 */
@SpringBootTest
@ActiveProfiles("test") // Use test profile if you have specific test configurations
public class HashingServiceTest {

    @Autowired
    private HashingService hashingService;

    /**
     * Tests that a password can be hashed and then verified with the same service.
     */
    @Test
    public void testHashAndCheckPassword() {
        // Given
        String rawPassword = "securePassword123!";

        // When
        String hashedPassword = hashingService.hashPassword(rawPassword);

        // Then
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(rawPassword, hashedPassword, "Hashed password should be different from raw password");
        assertTrue(hashingService.checkPassword(rawPassword, hashedPassword),
                "Original password should validate against the hash");
    }

    /**
     * Tests that an incorrect password fails validation against a hash.
     */
    @Test
    public void testIncorrectPasswordFails() {
        // Given
        String correctPassword = "correctPassword123!";
        String incorrectPassword = "wrongPassword123!";

        // When
        String hashedPassword = hashingService.hashPassword(correctPassword);

        // Then
        assertFalse(hashingService.checkPassword(incorrectPassword, hashedPassword),
                "Incorrect password should not validate against the hash");
    }

    /**
     * Tests that different salt rounds produce different hashes.
     * This is more of a BCrypt behavior test but useful to verify our configuration.
     */
    @Test
    public void testMultipleHashesAreDifferent() {
        // Given
        String password = "testPassword123!";

        // When
        String firstHash = hashingService.hashPassword(password);
        String secondHash = hashingService.hashPassword(password);

        // Then
        assertNotEquals(firstHash, secondHash,
                "Multiple hashes of the same password should be different due to different salts");

        // But both should validate
        assertTrue(hashingService.checkPassword(password, firstHash),
                "First hash should validate against the original password");
        assertTrue(hashingService.checkPassword(password, secondHash),
                "Second hash should validate against the original password");
    }

    /**
     * Tests handling of edge cases like empty or null passwords.
     */
    @Test
    public void testEdgeCases() {
        // Given
        String emptyPassword = "";

        // When
        String emptyHash = hashingService.hashPassword(emptyPassword);

        // Then
        assertNotNull(emptyHash, "Hashing an empty string should still produce a hash");
        assertTrue(hashingService.checkPassword(emptyPassword, emptyHash),
                "Empty password should validate against its hash");
        
    }
}