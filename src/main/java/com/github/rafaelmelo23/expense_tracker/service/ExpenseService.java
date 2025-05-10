package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dto.auth.FirstRegistryDTO;
import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseByMonthDTO;
import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseDTO;
import com.github.rafaelmelo23.expense_tracker.exception.ExpenseException;
import com.github.rafaelmelo23.expense_tracker.exception.UserException;
import com.github.rafaelmelo23.expense_tracker.model.Expense;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.UserAccounting;
import com.github.rafaelmelo23.expense_tracker.model.dao.ExpenseDAO;
import com.github.rafaelmelo23.expense_tracker.model.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.model.dao.UserAccountingDAO;
import com.github.rafaelmelo23.expense_tracker.model.interfaces.UserSalaryInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing expense-related operations, including
 * initial user registration with expenses and accounting details, persisting new
 * expenses, retrieving expense data, and scheduling recurring financial updates.
 */
@Service
public class ExpenseService {

    private final ExpenseDAO expenseDAO;
    private final UserService userService;
    private final UserAccountingDAO userAccountingDAO;
    private final LocalUserDAO localUserDAO;

    /**
     * Represents the current year for filtering expenses.
     */
    private static final int currentYear = LocalDate.now().getYear();

    /**
     * Represents the start of the current year (January 1st, 00:00).
     */
    private final LocalDateTime startOfTheYear = LocalDateTime.of(currentYear, 1, 1, 0, 0);

    /**
     * Represents the end of the current year (December 31st, 23:59).
     */
    private final LocalDateTime endOfTheYear = LocalDateTime.of(currentYear, 12, 31, 23, 59);

    /**
     * Constructor for the ExpenseService, injecting necessary DAOs and services.
     *
     * @param expenseDAO        Data Access Object for Expense entities.
     * @param userService       Service for user-related operations.
     * @param userAccountingDAO Data Access Object for UserAccounting entities.
     * @param localUserDAO      Data Access Object for LocalUser entities.
     */
    public ExpenseService(ExpenseDAO expenseDAO, UserService userService, UserAccountingDAO userAccountingDAO, LocalUserDAO localUserDAO) {
        this.expenseDAO = expenseDAO;
        this.userService = userService;
        this.userAccountingDAO = userAccountingDAO;
        this.localUserDAO = localUserDAO;
    }

    /**
     * Handles the initial registration of a user, including setting their first login
     * status to false, persisting their initial accounting details, and saving their
     * initial expenses.
     *
     * @param registryDTO DTO containing the user's initial registration information,
     * including accounting details and a list of initial expenses.
     * @throws ExpenseException.InvalidExpenseDataException if the provided registry DTO is null
     * or if any initial expense is missing an amount.
     * @throws ExpenseException.PersistenceException      if there is an error during the persistence of
     * user accounting or expenses.
     */
    public void firstRegistry(FirstRegistryDTO registryDTO) {
        LocalUser user = userService.getAuthenticatedUser();

        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }
        if (registryDTO == null) {
            throw new ExpenseException.InvalidExpenseDataException("first registry payload is null");
        }

        localUserDAO.setUserFirstLoginToFalse(user.getId());

        UserAccounting accounting = persistUserAccounting(registryDTO, user);

        List<Expense> expenses = persistExpenses(registryDTO.getExpenses(), user);

        try {
            userAccountingDAO.save(accounting);
            expenseDAO.saveAll(expenses);
        } catch (DataAccessException dae) {
            throw new ExpenseException.PersistenceException("persist initial registry", dae);
        }
    }

    /**
     * Converts a list of ExpenseDTOs into Expense entities, associating them with the given user.
     *
     * @param expenseDTOs List of Expense Data Transfer Objects representing the initial expenses.
     * @param user        The LocalUser to whom these expenses belong.
     * @return A list of persisted Expense entities.
     * @throws ExpenseException.InvalidExpenseDataException if any of the provided ExpenseDTOs is missing an expense amount.
     */
    private List<Expense> persistExpenses(List<ExpenseDTO> expenseDTOs, LocalUser user) {
        return expenseDTOs.stream().map(e -> {
            if (e.getExpenseAmount() == null) {
                throw new ExpenseException.InvalidExpenseDataException("one of the initial expenses is missing amount");
            }

            Expense exp = new Expense();
            exp.setUser(user);
            exp.setName(e.getExpenseName());
            exp.setDescription(e.getDescription());
            exp.setCategory(e.getExpenseCategory());
            exp.setAmount(e.getExpenseAmount());
            exp.setDate(e.getExpenseDate());
            exp.setIsRecurrent(e.getIsRecurrent());
            return exp;
        }).toList();
    }

    /**
     * Creates and populates a UserAccounting entity based on the provided FirstRegistryDTO and user.
     *
     * @param dto  Data Transfer Object containing the user's initial accounting details.
     * @param user The LocalUser to whom this accounting information belongs.
     * @return The created and populated UserAccounting entity.
     */
    private UserAccounting persistUserAccounting(FirstRegistryDTO dto, LocalUser user) {
        UserAccounting accounting = new UserAccounting();
        accounting.setUser(user);
        accounting.setSalaryDate(dto.getSalaryDate());
        accounting.setMonthlySalary(dto.getMonthlySalary());
        accounting.setCurrentBalance(dto.getCurrentBalance());
        return accounting;
    }

    /**
     * Persists a new expense for the currently authenticated user and updates their current balance.
     *
     * @param expenseDTO Data Transfer Object containing the details of the expense to be persisted.
     * @return The new current balance of the user after the expense is recorded.
     * @throws UserException.UserNotAuthenticatedException if no user is currently authenticated.
     * @throws ExpenseException.UserAccountingNotFoundException if the UserAccounting for the authenticated user is not found.
     * @throws ExpenseException.InvalidExpenseDataException if the provided expense DTO or its amount is null.
     * @throws ExpenseException.PersistenceException      if there is an error during the persistence of
     * the expense or the update of the user's balance.
     */
    public BigDecimal persistExpense(ExpenseDTO expenseDTO) {
        LocalUser user = userService.getAuthenticatedUser();

        if (user == null || SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        UserAccounting userAccounting = userAccountingDAO.findByUser_Id(user.getId())
                .orElseThrow(ExpenseException.UserAccountingNotFoundException::new);

        if (expenseDTO == null || expenseDTO.getExpenseAmount() == null) {
            throw new ExpenseException.InvalidExpenseDataException("expenseDTO or amount is null");
        }

        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getExpenseAmount());
        expense.setUser(user);
        expense.setDate(expenseDTO.getExpenseDate());
        expense.setIsRecurrent(expenseDTO.getIsRecurrent());
        if (StringUtils.hasText(expenseDTO.getDescription())) {
            expense.setDescription(expenseDTO.getDescription());
        }
        if (expenseDTO.getExpenseCategory() != null) {
            expense.setCategory(expenseDTO.getExpenseCategory());
        }
        if (StringUtils.hasText(expenseDTO.getExpenseName())) {
            expense.setName(expenseDTO.getExpenseName());
        }

        BigDecimal newBalance = userAccounting.getCurrentBalance().subtract(expense.getAmount());
        userAccounting.setCurrentBalance(newBalance);

        try {
            expenseDAO.save(expense);
            userAccountingDAO.save(userAccounting);
        } catch (DataAccessException dae) {
            throw new ExpenseException.PersistenceException("persist expense and update balance", dae);
        }

        return newBalance;
    }

    /**
     * Retrieves all relevant expenses for the currently authenticated user within the current year.
     *
     * @return A list of ExpenseDTOs representing the user's expenses for the current year.
     * @throws UserException.UserNotAuthenticatedException if no user is currently authenticated.
     * @throws ExpenseException.ExpenseNotFoundException    if no expenses are found for the authenticated user
     * within the current year.
     */
    public List<ExpenseDTO> getAllExpenses() {
        LocalUser user = userService.getAuthenticatedUser();
        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        // Find all expenses for the user within the defined date range for the current year.
        List<Expense> expensesList = expenseDAO.findRelevantExpenses(user.getId(), startOfTheYear, endOfTheYear);
        if (expensesList == null) {
            throw new ExpenseException.ExpenseNotFoundException(null);
        }

        return expensesList.stream()
                .map(ExpenseDTO::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the yearly expenses for the currently authenticated user, grouped by month.
     *
     * @return An ExpenseByMonthDTO containing a map where each month of the year is a key,
     * and the value is a list of ExpenseDTOs for that month.
     * @throws UserException.UserNotAuthenticatedException if no user is currently authenticated.
     */
    public ExpenseByMonthDTO getYearlyExpensesByMonth() {
        LocalUser user = userService.getAuthenticatedUser();
        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        List<Expense> expenses = expenseDAO.findRelevantExpenses(user.getId(), startOfTheYear, endOfTheYear);

        Map<Month, List<Expense>> groupedExpenses =
                expenses.stream()
                        .collect(Collectors.groupingBy(
                                expense -> expense.getDate().getMonth(),
                                () -> new EnumMap<>(Month.class),
                                Collectors.toList()
                        ));

        Map<Month, List<ExpenseDTO>> dtoMap = groupedExpenses.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        monthListEntry -> monthListEntry.getValue()
                                .stream()
                                .map(ExpenseDTO::toDTO)
                                .collect(Collectors.toList()),
                        (a, b) -> a,
                        () -> new EnumMap<>(Month.class)
                ));

        ExpenseByMonthDTO expenseByMonthDTO = new ExpenseByMonthDTO();
        dtoMap.forEach(expenseByMonthDTO::addExpenses);
        return expenseByMonthDTO;
    }

    /**
     * Scheduled task that runs every day at midnight to credit users' monthly salary
     * and deduct their total recurrent expenses. It identifies users whose salary date
     * matches the current day of the month.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
    public void creditMonthlySalaryMinusRecurrentExpenses() {
        int today = LocalDate.now().getDayOfMonth();

        List<UserSalaryInfo> salaryInfo = userAccountingDAO.findUserSalariesByDayOfTheMonth(today);

        salaryInfo.forEach(info -> calculateMonthlySalaryMinusRecurrentExpenses(info.getUserId(), info.getMonthlySalary()));
    }

    /**
     * Calculates the new balance for a user by adding their monthly salary and subtracting
     * the total of their recurrent expenses. Updates the user's current balance in the database.
     *
     * @param userId        The ID of the user whose balance needs to be updated.
     * @param monthlySalary The monthly salary of the user.
     */
    public void calculateMonthlySalaryMinusRecurrentExpenses(Long userId, BigDecimal monthlySalary) {

        List<BigDecimal> expenses = expenseDAO.findRecurrentExpensesByUser(userId);

        BigDecimal totalExpenses = expenses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newBalance = monthlySalary.subtract(totalExpenses);

        expenseDAO.monthlyBalanceUpdate(userId, newBalance);
    }
}