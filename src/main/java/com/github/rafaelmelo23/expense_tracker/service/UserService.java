package com.github.rafaelmelo23.expense_tracker.service;


import com.github.rafaelmelo23.expense_tracker.api.controller.authentication.body.RegistrationBody;
import com.github.rafaelmelo23.expense_tracker.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final LocalUserDAO localUserDAO;
    private final HashingService hashingService;

    public UserService(LocalUserDAO localUserDAO, HashingService hashingService) {
        this.localUserDAO = localUserDAO;
        this.hashingService = hashingService;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) {

        if (localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setPassword(hashingService.hashPassword(registrationBody.getPassword()));

        return localUserDAO.save(user);
    }

    public String loginUser(String email, String password) {
        return null;
    }

}
