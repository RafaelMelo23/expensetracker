package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dao.ExpenseDAO;
import com.github.rafaelmelo23.expense_tracker.dao.UserAccountingDAO;
import com.github.rafaelmelo23.expense_tracker.dto.auth.FirstRegistryDTO;
import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseDTO;
import com.github.rafaelmelo23.expense_tracker.model.Expense;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.UserAccounting;
import com.github.rafaelmelo23.expense_tracker.model.interfaces.UserSalaryInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseDAO expenseDAO;
    private final UserService userService;
    private static final int currentYear = LocalDate.now().getYear();
    private final UserAccountingDAO userAccountingDAO;
    LocalDateTime startOfTheYear = LocalDateTime.of(currentYear, 1, 1, 0, 0);
    LocalDateTime endOfTheYear = LocalDateTime.of(currentYear, 12, 31, 23, 59);

    public ExpenseService(ExpenseDAO expenseDAO, UserService userService, UserAccountingDAO userAccountingDAO) {
        this.expenseDAO = expenseDAO;
        this.userService = userService;
        this.userAccountingDAO = userAccountingDAO;
    }

    public void firstRegistry(FirstRegistryDTO registryDTO) {

        LocalUser user = userService.getAuthenticatedUser();

        UserAccounting newUserAccounting = new UserAccounting();

        newUserAccounting.setUser(user);
        newUserAccounting.setSalaryDate(registryDTO.getSalaryDate());
        newUserAccounting.setMonthlySalary(registryDTO.getMonthlySalary());
        newUserAccounting.setCurrentBalance(registryDTO.getCurrentBalance());

        List<Expense> newExpensesList = new ArrayList<>();
        registryDTO.getExpenses().forEach(expense -> {
            Expense newExpense = new Expense();
            newExpense.setName(expense.getExpenseName());
            newExpense.setDescription(expense.getDescription());
            newExpense.setUser(user);
            newExpense.setCategory(expense.getExpenseCategory());
            newExpense.setAmount(expense.getExpenseAmount());
            newExpense.setDate(expense.getExpenseDate());
            newExpensesList.add(newExpense);
        });

        userAccountingDAO.save(newUserAccounting);
        expenseDAO.saveAll(newExpensesList);
    }

    public BigDecimal persistExpense(ExpenseDTO expenseDTO) {
        LocalUser user = userService.getAuthenticatedUser();

        UserAccounting userAccounting = userAccountingDAO.findByUser_Id(user.getId());

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
        expenseDAO.save(expense);
        userAccountingDAO.save(userAccounting);

        return newBalance;
    }

    public List<ExpenseDTO> getAllExpenses() {

        LocalUser user = userService.getAuthenticatedUser();

        List<Expense> expensesList = expenseDAO.findRelevantExpenses(user.getId(), startOfTheYear, endOfTheYear);

        return expensesList.stream()
                .map(ExpenseDTO::toDTO)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void creditMonthlySalaryMinusRecurrentExpenses() {
        int today = LocalDate.now().getDayOfMonth();

        List<UserSalaryInfo> salaryInfo = userAccountingDAO.findUserSalariesByDayOfTheMonth(today);

        salaryInfo.stream()
                .forEach(info -> {
                    Long userId = info.getUserId();
                    BigDecimal monthlySalary = info.getMonthlySalary();

                    calculateMonthlySalaryMinusRecurrentExpenses(userId, monthlySalary);
                });
    }

    public void calculateMonthlySalaryMinusRecurrentExpenses(Long userId, BigDecimal monthlySalary) {

        List<BigDecimal> expenses = expenseDAO.findRecurrentExpensesByUser(userId);

        BigDecimal totalExpenses = expenses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newBalance = totalExpenses.subtract(monthlySalary);

        expenseDAO.monthlyBalanceUpdate(userId, newBalance);
    }
}
