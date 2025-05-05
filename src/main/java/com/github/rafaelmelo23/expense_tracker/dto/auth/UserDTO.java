package com.github.rafaelmelo23.expense_tracker.dto.auth;

import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import lombok.Data;

@Data
public class UserDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String jwtToken;

    public UserDTO toDTO(LocalUser localUser) {
        UserDTO dto = new UserDTO();

        dto.setFirstName(localUser.getFirstName());
        dto.setLastName(localUser.getLastName());
        dto.setEmail(localUser.getEmail());
        return dto;
    }
}
