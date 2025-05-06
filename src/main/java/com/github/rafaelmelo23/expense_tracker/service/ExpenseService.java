package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.model.dao.ExpenseDAO;
import com.github.rafaelmelo23.expense_tracker.model.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.model.dao.UserAccountingDAO;
import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseByMonthDTO;
import com.github.rafaelmelo23.expense_tracker.dto.auth.FirstRegistryDTO;
import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseDTO;
import com.github.rafaelmelo23.expense_tracker.exception.ExpenseException;
import com.github.rafaelmelo23.expense_tracker.exception.UserException;
import com.github.rafaelmelo23.expense_tracker.model.Expense;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.UserAccounting;
import com.github.rafaelmelo23.expense_tracker.model.interfaces.UserSalaryInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
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

@Service
public class ExpenseService {

    private final ExpenseDAO expenseDAO;
    private final UserService userService;
    private final UserAccountingDAO userAccountingDAO;
    private static final int currentYear = LocalDate.now().getYear();
    private final LocalDateTime startOfTheYear = LocalDateTime.of(currentYear, 1, 1, 0, 0);
    private final LocalDateTime endOfTheYear = LocalDateTime.of(currentYear, 12, 31, 23, 59);
    private final LocalUserDAO localUserDAO;

    public ExpenseService(ExpenseDAO expenseDAO, UserService userService, UserAccountingDAO userAccountingDAO, LocalUserDAO localUserDAO) {
        this.expenseDAO = expenseDAO;
        this.userService = userService;
        this.userAccountingDAO = userAccountingDAO;
        this.localUserDAO = localUserDAO;
    }

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

    private UserAccounting persistUserAccounting(FirstRegistryDTO dto, LocalUser user) {
        UserAccounting accounting = new UserAccounting();
        accounting.setUser(user);
        accounting.setSalaryDate(dto.getSalaryDate());
        accounting.setMonthlySalary(dto.getMonthlySalary());
        accounting.setCurrentBalance(dto.getCurrentBalance());
        return accounting;
    }

    public BigDecimal persistExpense(ExpenseDTO expenseDTO) {
        LocalUser user = userService.getAuthenticatedUser();
        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        UserAccounting userAccounting = userAccountingDAO.findByUser_Id(user.getId());

        if (userAccounting == null) {
            throw new ExpenseException.UserAccountingNotFoundException(user.getId());
        }
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

    public List<ExpenseDTO> getAllExpenses() {
        LocalUser user = userService.getAuthenticatedUser();
        if (user == null) {
            throw new UserException.UserNotAuthenticatedException();
        }

        List<Expense> expensesList = expenseDAO.findRelevantExpenses(user.getId(), startOfTheYear, endOfTheYear);
        if (expensesList == null) {
            throw new ExpenseException.ExpenseNotFoundException(null);
        }

        return expensesList.stream()
                .map(ExpenseDTO::toDTO)
                .collect(Collectors.toList());
    }

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
                        monthListEntry ->  monthListEntry.getValue()
                                .stream()
                                .map(ExpenseDTO::toDTO)
                                .collect(Collectors.toList()),
                    (a,b) -> a,
                    () -> new EnumMap<>(Month.class)
                ));

        ExpenseByMonthDTO expenseByMonthDTO = new ExpenseByMonthDTO();
        dtoMap.forEach(expenseByMonthDTO::addExpenses);
        return expenseByMonthDTO;

    }

    @Scheduled(cron = "0 0 0 * * *")
    public void creditMonthlySalaryMinusRecurrentExpenses() {
        int today = LocalDate.now().getDayOfMonth();
        List<UserSalaryInfo> salaryInfo = userAccountingDAO.findUserSalariesByDayOfTheMonth(today);
        salaryInfo.forEach(info -> calculateMonthlySalaryMinusRecurrentExpenses(info.getUserId(), info.getMonthlySalary()));
    }

    public void calculateMonthlySalaryMinusRecurrentExpenses(Long userId, BigDecimal monthlySalary) {
        List<BigDecimal> expenses = expenseDAO.findRecurrentExpensesByUser(userId);
        BigDecimal totalExpenses = expenses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newBalance = totalExpenses.subtract(monthlySalary);
        expenseDAO.monthlyBalanceUpdate(userId, newBalance);
    }
}