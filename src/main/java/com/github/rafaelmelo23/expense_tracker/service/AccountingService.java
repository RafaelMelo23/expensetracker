package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dto.expense.UserAdditionsDTO;
import com.github.rafaelmelo23.expense_tracker.exception.UserException;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.dao.UserAccountingDAO;
import com.github.rafaelmelo23.expense_tracker.model.UserAdditionsLog;
import com.github.rafaelmelo23.expense_tracker.model.dao.UserAdditionsLogDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountingService {

    private final UserAdditionsLogDAO userAdditionsLog;
    private final UserService userService;
    private final UserAccountingDAO userAccountingDAO;
    private final UserAdditionsLogDAO userAdditionsLogDAO;

    public List<UserAdditionsDTO> getAllYearAdditions(int year) {
        LocalDateTime startOfTheYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfTheYear = LocalDateTime.of(year, 12, 31, 23, 59);

        LocalUser user = userService.getAuthenticatedUser();

        List<UserAdditionsLog> additions = userAdditionsLog.findByCreatedAtBetweenAndUserId(startOfTheYear, endOfTheYear, user.getId());

        return additions.stream()
                .map(UserAdditionsDTO::toDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal addToBalance(UserAdditionsDTO dto) throws AccessDeniedException {

        LocalUser user = userService.getAuthenticatedUser();
        if (user == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        userAccountingDAO.addToBalance(dto.getAmount(), user.getId());

        UserAdditionsLog userAdditionsLog = new UserAdditionsLog();
        userAdditionsLog.setCreatedAt(LocalDateTime.now());
        userAdditionsLog.setUser(user);
        userAdditionsLog.setAmount(dto.getAmount());
        userAdditionsLog.setDescription(dto.getDescription());
        userAdditionsLogDAO.save(userAdditionsLog);

        return userAccountingDAO.findCurrentBalanceByUser_Id(user.getId());
    }

    public BigDecimal getBalance() {
        return userAccountingDAO.findCurrentBalanceByUser_Id(userService.getAuthenticatedUser().getId());
    }

    public void updateSalaryAmount(BigDecimal salaryAmount) {
        LocalUser user = userService.getAuthenticatedUser();

        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        userAccountingDAO.updateUserSalary(salaryAmount, user.getId());
    }

    public void updateSalaryDate(BigDecimal salaryDate) {
        LocalUser user = userService.getAuthenticatedUser();

        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        userAccountingDAO.updateUserSalaryDate(salaryDate, user.getId());
    }

}
