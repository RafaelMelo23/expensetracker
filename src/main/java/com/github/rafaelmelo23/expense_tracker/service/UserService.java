package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dto.auth.UserDTO;
import com.github.rafaelmelo23.expense_tracker.exception.UserException;
import com.github.rafaelmelo23.expense_tracker.model.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.dto.auth.RegistrationBody;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

/**
 * Service class that handles user management operations such as registration,
 * authentication, and retrieving user information.
 */
@Service
public class UserService {

    private final LocalUserDAO localUserDAO;
    private final HashingService hashingService;
    private final JWTService jwt;
    Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Constructs a new UserService with required dependencies.
     *
     * @param localUserDAO    DAO for user persistence operations
     * @param hashingService  Service to handle password hashing
     * @param jwt             Service to handle JWT operations
     */
    public UserService(LocalUserDAO localUserDAO, HashingService hashingService, JWTService jwt) {
        this.localUserDAO = localUserDAO;
        this.hashingService = hashingService;
        this.jwt = jwt;
    }

    /**
     * Registers a new user in the system.
     *
     * @param registrationBody  Contains user registration details
     * @throws IllegalArgumentException if the email is already in use
     */
    public void registerUser(RegistrationBody registrationBody) {
        if (localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setPassword(hashingService.hashPassword(registrationBody.getPassword()));
        user.setRole(Role.ROLE_USER);

        localUserDAO.save(user);
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param email       User's email address
     * @param rawPassword User's password in plain text
     * @return UserDTO containing user email and JWT token
     * @throws IllegalArgumentException if credentials are invalid or user not found
     */
    public UserDTO loginUser(String email, String rawPassword)  {
        LocalUser user = localUserDAO.findByEmailIgnoreCase(email.trim())
                .orElseThrow(UserException.UserNotFoundException::new);

        String raw = (rawPassword == null ? "" : rawPassword.trim());

        boolean ok = hashingService.checkPassword(raw, user.getPassword());

        if (!ok) {
            throw new UserException.UserInvalidAuthenticationException();
        }

        String jwtToken = jwt.generateJWT(user);
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setJwtToken(jwtToken);

        return userDTO;
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return The authenticated LocalUser object
     * @throws IllegalArgumentException if authentication is invalid
     */
    public LocalUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserException.UserNotAuthenticatedException();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LocalUser) {
            return (LocalUser) principal;
        } else {
            throw new UserException.UserNotAuthenticatedException();
        }
    }
}