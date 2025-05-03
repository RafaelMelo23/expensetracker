package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.dto.auth.RegistrationBody;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final LocalUserDAO localUserDAO;
    private final HashingService hashingService;
    private final JWTService jwt;

    public UserService(LocalUserDAO localUserDAO, HashingService hashingService, JWTService jwt) {
        this.localUserDAO = localUserDAO;
        this.hashingService = hashingService;
        this.jwt = jwt;
    }

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

    public String loginUser(String email, String hashedPassword) {

        String trimmedEmail = email.trim();

        LocalUser user = localUserDAO.findByEmailIgnoreCase(trimmedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (hashingService.checkPassword(hashedPassword, user.getPassword())) {
            return jwt.generateJWT(user);
        }

        throw new IllegalArgumentException("Invalid password");
    }

    public LocalUser getAuthenticatedUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Invalid Authentication");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LocalUser) {
            return (LocalUser) principal;
        }

        throw new IllegalArgumentException("User not authenticated");
    }

}
