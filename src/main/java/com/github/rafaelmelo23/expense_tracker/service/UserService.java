package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dto.auth.UserDTO;
import com.github.rafaelmelo23.expense_tracker.model.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.dto.auth.RegistrationBody;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final LocalUserDAO localUserDAO;
    private final HashingService hashingService;
    private final JWTService jwt;
    Logger logger = LoggerFactory.getLogger(UserService.class);

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

    public UserDTO loginUser(String email, String rawPassword) {

        LocalUser user = localUserDAO.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String raw = (rawPassword == null ? "" : rawPassword.trim());

        System.out.printf("Comparing raw '%s' against hash '%s'%n", raw, user.getPassword());

        boolean ok = hashingService.checkPassword(raw, user.getPassword());
        System.out.println("Password match? " + ok);

        if (!ok) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String jwtToken = jwt.generateJWT(user);
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setJwtToken(jwtToken);

        return userDTO;
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
