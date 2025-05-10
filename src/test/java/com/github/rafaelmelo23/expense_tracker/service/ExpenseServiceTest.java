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
import com.github.rafaelmelo23.expense_tracker.model.enums.ExpenseCategory;
import com.github.rafaelmelo23.expense_tracker.model.interfaces.UserSalaryInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ExpenseServiceTest {

    @InjectMocks
    private ExpenseService expenseService;

    @Mock
    private UserService userService;

    @Mock
    private ExpenseDAO expenseDAO;

    @Mock
    private UserAccountingDAO userAccountingDAO;

    @Mock
    private LocalUserDAO localUserDAO;

    private LocalUser testUser;
    private UserAccounting testUserAccounting;
    private ExpenseDTO testExpenseDTO;
    private FirstRegistryDTO testFirstRegistryDTO;

    @BeforeEach
    public void setup() {

        testUser = new LocalUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");


        testUserAccounting = new UserAccounting();
        testUserAccounting.setId(1L);
        testUserAccounting.setUser(testUser);
        testUserAccounting.setMonthlySalary(new BigDecimal("5000.00"));
        testUserAccounting.setCurrentBalance(new BigDecimal("3000.00"));
        testUserAccounting.setSalaryDate(LocalDate.now().getDayOfMonth());


        testExpenseDTO = new ExpenseDTO();
        testExpenseDTO.setExpenseName("Test Expense");
        testExpenseDTO.setDescription("Test Description");
        testExpenseDTO.setExpenseAmount(new BigDecimal("100.00"));
        testExpenseDTO.setExpenseCategory(ExpenseCategory.FOOD);
        testExpenseDTO.setExpenseDate(LocalDateTime.now());
        testExpenseDTO.setIsRecurrent(false);


        testFirstRegistryDTO = new FirstRegistryDTO();
        testFirstRegistryDTO.setMonthlySalary(new BigDecimal("5000.00"));
        testFirstRegistryDTO.setCurrentBalance(new BigDecimal("3000.00"));
        testFirstRegistryDTO.setSalaryDate(LocalDate.now().getDayOfMonth());
        List<ExpenseDTO> initialExpenses = new ArrayList<>();
        initialExpenses.add(testExpenseDTO);
        testFirstRegistryDTO.setExpenses(initialExpenses);


        setupSecurityContext();

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(userAccountingDAO.findByUser_Id(anyLong())).thenReturn(Optional.ofNullable(testUserAccounting));
    }

    private void setupSecurityContext() {

        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, new ArrayList<>());


        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testFirstRegistry_Success() {

        doNothing().when(localUserDAO).setUserFirstLoginToFalse(anyLong());


        setupSecurityContext();


        expenseService.firstRegistry(testFirstRegistryDTO);


        verify(localUserDAO).setUserFirstLoginToFalse(testUser.getId());
        verify(userAccountingDAO).save(any(UserAccounting.class));
        verify(expenseDAO).saveAll(anyList());
    }

    @Test
    public void testFirstRegistry_UserNotAuthenticated() {
        when(userService.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UserException.UserNotAuthenticatedException.class, () -> {
            expenseService.firstRegistry(testFirstRegistryDTO);
        });

        verifyNoInteractions(localUserDAO);
        verifyNoInteractions(userAccountingDAO);
        verifyNoInteractions(expenseDAO);
    }

    @Test
    public void testFirstRegistry_NullPayload() {

        assertThrows(ExpenseException.InvalidExpenseDataException.class, () -> {
            expenseService.firstRegistry(null);
        });

        verifyNoInteractions(localUserDAO);
        verifyNoInteractions(userAccountingDAO);
        verifyNoInteractions(expenseDAO);
    }

    @Test
    public void testFirstRegistry_PersistenceException() {

        doNothing().when(localUserDAO).setUserFirstLoginToFalse(anyLong());
        when(userAccountingDAO.save(any(UserAccounting.class))).thenThrow(new DataAccessException("Test exception") {});


        assertThrows(ExpenseException.PersistenceException.class, () -> {
            expenseService.firstRegistry(testFirstRegistryDTO);
        });
    }

    @Test
    public void testPersistExpense_Success() {

        BigDecimal expectedNewBalance = new BigDecimal("2900.00"); // 3000 - 100


        BigDecimal actualNewBalance = expenseService.persistExpense(testExpenseDTO);


        assertEquals(expectedNewBalance, actualNewBalance);
        verify(expenseDAO).save(any(Expense.class));
        verify(userAccountingDAO).save(any(UserAccounting.class));
    }

    @Test
    public void testPersistExpense_UserNotAuthenticated() {
        SecurityContextHolder.clearContext();


        assertThrows(UserException.UserNotAuthenticatedException.class, () -> {
            expenseService.persistExpense(testExpenseDTO);
        });

        verifyNoInteractions(expenseDAO);
        verify(userAccountingDAO, never()).save(any(UserAccounting.class));
    }

    @Test
    public void testPersistExpense_UserAccountingNotFound() {

        when(userAccountingDAO.findByUser_Id(anyLong())).thenReturn(Optional.empty());

        assertThrows(ExpenseException.UserAccountingNotFoundException.class, () -> {
            expenseService.persistExpense(testExpenseDTO);
        });

        verifyNoInteractions(expenseDAO);
    }

    @Test
    public void testPersistExpense_NullExpenseOrAmount() {

        assertThrows(ExpenseException.InvalidExpenseDataException.class, () -> {
            expenseService.persistExpense(null);
        });


        ExpenseDTO nullAmountExpense = new ExpenseDTO();
        nullAmountExpense.setExpenseName("Test");
        nullAmountExpense.setExpenseAmount(null);


        assertThrows(ExpenseException.InvalidExpenseDataException.class, () -> {
            expenseService.persistExpense(nullAmountExpense);
        });

        verifyNoInteractions(expenseDAO);
        verify(userAccountingDAO, never()).save(any(UserAccounting.class));
    }

    @Test
    public void testPersistExpense_PersistenceException() {

        when(expenseDAO.save(any(Expense.class))).thenThrow(new DataAccessException("Test exception") {});


        assertThrows(ExpenseException.PersistenceException.class, () -> {
            expenseService.persistExpense(testExpenseDTO);
        });
    }

    @Test
    public void testGetAllExpenses_Success() {

        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setUser(testUser);
        expense1.setName("Expense 1");
        expense1.setAmount(new BigDecimal("100.00"));
        expense1.setDate(LocalDateTime.now());
        expense1.setCategory(ExpenseCategory.FOOD);

        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setUser(testUser);
        expense2.setName("Expense 2");
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setDate(LocalDateTime.now());
        expense2.setCategory(ExpenseCategory.TRANSPORT);

        List<Expense> expenses = Arrays.asList(expense1, expense2);

        when(expenseDAO.findRelevantExpenses(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expenses);


        List<ExpenseDTO> result = expenseService.getAllExpenses();


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Expense 1", result.get(0).getExpenseName());
        assertEquals("Expense 2", result.get(1).getExpenseName());
    }

    @Test
    public void testGetAllExpenses_UserNotAuthenticated() {

        when(userService.getAuthenticatedUser()).thenReturn(null);
        SecurityContextHolder.clearContext();


        assertThrows(UserException.UserNotAuthenticatedException.class, () -> {
            expenseService.getAllExpenses();
        });

        verifyNoInteractions(expenseDAO);
    }

    @Test
    public void testGetAllExpenses_ExpenseNotFound() {

        when(expenseDAO.findRelevantExpenses(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);


        assertThrows(ExpenseException.ExpenseNotFoundException.class, () -> {
            expenseService.getAllExpenses();
        });
    }

    @Test
    public void testGetYearlyExpensesByMonth_Success() {

        Expense januaryExpense = new Expense();
        januaryExpense.setId(1L);
        januaryExpense.setUser(testUser);
        januaryExpense.setName("January Expense");
        januaryExpense.setAmount(new BigDecimal("100.00"));
        januaryExpense.setDate(LocalDateTime.of(LocalDate.now().getYear(), 1, 15, 0, 0));

        Expense februaryExpense = new Expense();
        februaryExpense.setId(2L);
        februaryExpense.setUser(testUser);
        februaryExpense.setName("February Expense");
        februaryExpense.setAmount(new BigDecimal("200.00"));
        februaryExpense.setDate(LocalDateTime.of(LocalDate.now().getYear(), 2, 15, 0, 0));

        List<Expense> expenses = Arrays.asList(januaryExpense, februaryExpense);

        when(expenseDAO.findRelevantExpenses(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expenses);


        ExpenseByMonthDTO result = expenseService.getYearlyExpensesByMonth();

        assertNotNull(result);
        assertNotNull(result.getMonthlyExpenses().get(Month.JANUARY));
        assertNotNull(result.getMonthlyExpenses().get(Month.FEBRUARY));
        assertEquals(1, result.getMonthlyExpenses().get(Month.JANUARY).size());
        assertEquals(1, result.getMonthlyExpenses().get(Month.FEBRUARY).size());
        assertEquals("January Expense", result.getMonthlyExpenses().get(Month.JANUARY).get(0).getExpenseName());
        assertEquals("February Expense", result.getMonthlyExpenses().get(Month.FEBRUARY).get(0).getExpenseName());
    }

    @Test
    public void testGetYearlyExpensesByMonth_UserNotAuthenticated() {

        when(userService.getAuthenticatedUser()).thenReturn(null);
        SecurityContextHolder.clearContext();


        assertThrows(UserException.UserNotAuthenticatedException.class, () -> {
            expenseService.getYearlyExpensesByMonth();
        });

        verifyNoInteractions(expenseDAO);
    }

    @Test
    public void testCalculateMonthlySalaryMinusRecurrentExpenses() {

        List<BigDecimal> recurrentExpenses = new ArrayList<>(Arrays.asList(
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                new BigDecimal("300.00")
        ));

        Mockito.doReturn(recurrentExpenses).when(expenseDAO).findRecurrentExpensesByUser(Mockito.anyLong());

        Mockito.doNothing().when(expenseDAO).monthlyBalanceUpdate(Mockito.anyLong(), Mockito.any(BigDecimal.class));

        expenseService.calculateMonthlySalaryMinusRecurrentExpenses(1L, new BigDecimal("1000.00"));

        Mockito.verify(expenseDAO).monthlyBalanceUpdate(Mockito.eq(1L), Mockito.eq(new BigDecimal("400.00")));
    }

    @Test
    public void testCreditMonthlySalaryMinusRecurrentExpenses() {

        int today = LocalDate.now().getDayOfMonth();

        UserSalaryInfo mockSalaryInfo = mock(UserSalaryInfo.class);
        when(mockSalaryInfo.getUserId()).thenReturn(testUser.getId());
        when(mockSalaryInfo.getMonthlySalary()).thenReturn(testUserAccounting.getMonthlySalary());

        when(userAccountingDAO.findUserSalariesByDayOfTheMonth(today))
                .thenReturn(List.of(mockSalaryInfo));

        List<BigDecimal> expenses = Arrays.asList(new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("50.00"));
        BigDecimal totalExpenses = expenses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedNewBalance = mockSalaryInfo.getMonthlySalary().subtract(totalExpenses);

        when(expenseDAO.findRecurrentExpensesByUser(testUser.getId())).thenReturn(expenses);

        expenseService.creditMonthlySalaryMinusRecurrentExpenses();

        verify(userAccountingDAO).findUserSalariesByDayOfTheMonth(today);
        verify(expenseDAO).findRecurrentExpensesByUser(testUser.getId());
        verify(expenseDAO).monthlyBalanceUpdate(testUser.getId(), expectedNewBalance);
    }
}