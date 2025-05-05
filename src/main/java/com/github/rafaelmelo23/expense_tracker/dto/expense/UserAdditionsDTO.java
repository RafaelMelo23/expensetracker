package com.github.rafaelmelo23.expense_tracker.dto.expense;

import com.github.rafaelmelo23.expense_tracker.model.UserAdditionsLog;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserAdditionsDTO {

    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public static UserAdditionsDTO toDTO(UserAdditionsLog userAdditionsLog) {
        UserAdditionsDTO userAdditionsDTO = new UserAdditionsDTO();

        userAdditionsDTO.setAmount(userAdditionsLog.getAmount());
        userAdditionsDTO.setDescription(userAdditionsLog.getDescription());
        userAdditionsDTO.setCreatedAt(userAdditionsLog.getCreatedAt());

        return userAdditionsDTO;
    }
}
