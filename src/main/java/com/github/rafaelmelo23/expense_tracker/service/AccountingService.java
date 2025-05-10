package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dto.expense.UserAdditionsDTO;
import com.github.rafaelmelo23.expense_tracker.exception.UserException;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.UserAdditionsLog;
import com.github.rafaelmelo23.expense_tracker.model.dao.UserAccountingDAO;
import com.github.rafaelmelo23.expense_tracker.model.dao.UserAdditionsLogDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /**
     * Retrieves all additional balance entries for the authenticated user
     * within a specific year.
     *
     * @param year: the year to filter additions
     * @return list of {@link UserAdditionsDTO} representing each addition
     */
    public List<UserAdditionsDTO> getAllYearAdditions(int year) {
        LocalDateTime startOfTheYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfTheYear = LocalDateTime.of(year, 12, 31, 23, 59);

        LocalUser user = userService.getAuthenticatedUser();

        List<UserAdditionsLog> additions = userAdditionsLog.findByCreatedAtBetweenAndUserId(startOfTheYear, endOfTheYear, user.getId());

        return additions.stream()
                .map(UserAdditionsDTO::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adds a specified amount to the authenticated user's balance
     * and logs the addition in the database.
     *
     * @param dto the data transfer object containing amount and description
     * @return the updated balance after the addition
     * @throws AccessDeniedException if no user is authenticated
     */
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

    /**
     * Retrieves the current balance of the authenticated user.
     *
     * @return current balance as {@link BigDecimal}
     */
    public BigDecimal getBalance() {
        return userAccountingDAO.findCurrentBalanceByUser_Id(userService.getAuthenticatedUser().getId());
    }

    /**
     * Retrieves the monthly salary of the authenticated user.
     *
     * @return salary as {@link BigDecimal}
     */
    public BigDecimal getSalary() {
        return userAccountingDAO.findSalaryByUser_Id(userService.getAuthenticatedUser().getId());
    }

    /**
     * Calculates the percentage of the user's salary that has been spent.
     *
     * @return spent percentage (0 to 1), rounded to two decimal places
     */
    public BigDecimal getMonthlySpentPercent() {
        LocalUser user = userService.getAuthenticatedUser();
        BigDecimal salary         = userAccountingDAO.findSalaryByUser_Id(user.getId());
        BigDecimal currentBalance = userAccountingDAO.findCurrentBalanceByUser_Id(user.getId());
        BigDecimal spentSoFar     = salary.subtract(currentBalance);

        if (salary.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal percentage = spentSoFar
                .divide(salary, 2, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE)
                .max(BigDecimal.ZERO);

        return percentage;
    }

    /**
     * Updates the authenticated user's salary amount.
     *
     * @param salaryAmount the new salary amount
     * @throws UserException.UserNotAuthenticatedException if no user is authenticated
     */
    public void updateSalaryAmount(BigDecimal salaryAmount) {
        LocalUser user = userService.getAuthenticatedUser();

        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        userAccountingDAO.updateUserSalary(salaryAmount, user.getId());
    }

    /**
     * Updates the authenticated user's salary payment date.
     *
     * @param salaryDate the new salary date (e.g. 10 for 10th of the month)
     * @throws UserException.UserNotAuthenticatedException if no user is authenticated
     */
    public void updateSalaryDate(BigDecimal salaryDate) {
        LocalUser user = userService.getAuthenticatedUser();

        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        userAccountingDAO.updateUserSalaryDate(salaryDate, user.getId());
    }
}
