package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dto.auth.RegistrationBody;
import com.github.rafaelmelo23.expense_tracker.dto.auth.UserDTO;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private LocalUserDAO localUserDAO;

    @Autowired
    private HashingService hashingService;

    private RegistrationBody validRegistration;

    @BeforeEach
    void setUp() {
        // Clean security context before each test
        SecurityContextHolder.clearContext();

        // Setup valid registration data
        validRegistration = new RegistrationBody();
        validRegistration.setEmail("newuser@example.com");
        validRegistration.setFirstName("New");
        validRegistration.setLastName("User");
        validRegistration.setPassword("password123");
    }

    @Test
    @DisplayName("Register user - success")
    void registerUserSuccess() {
        // Act
        userService.registerUser(validRegistration);

        // Assert
        Optional<LocalUser> savedUser = localUserDAO.findByEmailIgnoreCase("newuser@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals("New", savedUser.get().getFirstName());
        assertEquals("User", savedUser.get().getLastName());
        assertTrue(hashingService.checkPassword("password123", savedUser.get().getPassword()));
        assertEquals(Role.ROLE_USER, savedUser.get().getRole());
    }

    @Test
    @DisplayName("Register user - duplicate email")
    void registerUserDuplicateEmail() {
        // Arrange - use email from existing test data
        validRegistration.setEmail("anakin@example.com");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(validRegistration);
        });
        assertEquals("Email is already in use", exception.getMessage());
    }

    @Test
    @DisplayName("Login user - success")
    void loginUserSuccess() {
        // Arrange - using password matching the hashed value in test data
        // Note: You'll need to replace this with an actual password that matches your test hash
        String password = "password"; // Assuming this matches $2a$10$hashedpassword
        String email = "anakin@example.com";

        // Temporary set a known password for test user
        LocalUser user = localUserDAO.findByEmailIgnoreCase(email).get();
        String hashedPassword = hashingService.hashPassword(password);
        user.setPassword(hashedPassword);
        localUserDAO.save(user);

        // Act
        UserDTO result = userService.loginUser(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertNotNull(result.getJwtToken());
    }

    @Test
    @DisplayName("Login user - wrong password")
    void loginUserWrongPassword() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser("anakin@example.com", "wrongpassword");
        });
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Login user - user not found")
    void loginUserNotFound() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser("nonexistent@example.com", "password");
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("Get authenticated user - success")
    void getAuthenticatedUserSuccess() {
            // Arrange
            LocalUser user = localUserDAO.findByEmailIgnoreCase("anakin@example.com").get();

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        LocalUser authenticatedUser = userService.getAuthenticatedUser();

        // Assert
        assertNotNull(authenticatedUser);
        assertEquals(user.getId(), authenticatedUser.getId());
        assertEquals(user.getEmail(), authenticatedUser.getEmail());
    }

    @Test
    @DisplayName("Get authenticated user - no authentication")
    void getAuthenticatedUserNoAuth() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getAuthenticatedUser();
        });
        assertEquals("Invalid Authentication", exception.getMessage());
    }
}