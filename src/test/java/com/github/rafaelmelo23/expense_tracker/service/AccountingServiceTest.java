package com.github.rafaelmelo23.expense_tracker.service;

import com.github.rafaelmelo23.expense_tracker.dto.expense.UserAdditionsDTO;
import com.github.rafaelmelo23.expense_tracker.exception.UserException;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.UserAccounting;
import com.github.rafaelmelo23.expense_tracker.model.UserAdditionsLog;
import com.github.rafaelmelo23.expense_tracker.model.dao.UserAccountingDAO;
import com.github.rafaelmelo23.expense_tracker.model.dao.UserAdditionsLogDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AccountingServiceTest {

    @InjectMocks
    private AccountingService accountingService;

    @Mock
    private UserAdditionsLogDAO userAdditionsLogDAO;

    @Mock
    private UserService userService;

    @Mock
    private UserAccountingDAO userAccountingDAO;

    private LocalUser testUser;
    private UserAccounting testUserAccounting;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new LocalUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testUserAccounting = new UserAccounting();
        testUserAccounting.setId(1L);
        testUserAccounting.setUser(testUser);
        testUserAccounting.setMonthlySalary(new BigDecimal("5000.00"));
        testUserAccounting.setCurrentBalance(new BigDecimal("3000.00"));
        testUserAccounting.setSalaryDate(10);

        setupSecurityContext();

        when(userService.getAuthenticatedUser()).thenReturn(testUser);
        when(userAccountingDAO.findByUser_Id(anyLong())).thenReturn(Optional.of(testUserAccounting));
    }

    private void setupSecurityContext() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetAllYearAdditions() {
        int year = 2024;
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59);

        List<UserAdditionsLog> additionsLogList = new ArrayList<>();
        UserAdditionsLog log1 = new UserAdditionsLog();
        log1.setAmount(new BigDecimal("100.00"));
        log1.setDescription("Addition 1");
        log1.setCreatedAt(LocalDateTime.of(year, 2, 1, 12, 0));
        log1.setUser(testUser);
        additionsLogList.add(log1);

        UserAdditionsLog log2 = new UserAdditionsLog();
        log2.setAmount(new BigDecimal("200.00"));
        log2.setDescription("Addition 2");
        log2.setCreatedAt(LocalDateTime.of(year, 5, 1, 12, 0));
        log2.setUser(testUser);
        additionsLogList.add(log2);

        when(userAdditionsLogDAO.findByCreatedAtBetweenAndUserId(startOfYear, endOfYear, testUser.getId())).thenReturn(additionsLogList);

        List<UserAdditionsDTO> result = accountingService.getAllYearAdditions(year);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("100.00"), result.get(0).getAmount());
        assertEquals("Addition 1", result.get(0).getDescription());
        assertEquals(new BigDecimal("200.00"), result.get(1).getAmount());
        assertEquals("Addition 2", result.get(1).getDescription());

        verify(userAdditionsLogDAO, times(1)).findByCreatedAtBetweenAndUserId(startOfYear, endOfYear, testUser.getId());
        verify(userService, times(1)).getAuthenticatedUser();
    }

    @Test
    public void testAddToBalance() throws AccessDeniedException {
        UserAdditionsDTO dto = new UserAdditionsDTO();
        dto.setAmount(new BigDecimal("500.00"));
        dto.setDescription("Test Addition");

        when(userAccountingDAO.findCurrentBalanceByUser_Id(testUser.getId())).thenReturn(new BigDecimal("3500.00"));

        BigDecimal newBalance = accountingService.addToBalance(dto);

        assertEquals(new BigDecimal("3500.00"), newBalance);

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, times(1)).addToBalance(dto.getAmount(), testUser.getId());
        verify(userAdditionsLogDAO, times(1)).save(any(UserAdditionsLog.class));
        verify(userAccountingDAO, times(1)).findCurrentBalanceByUser_Id(testUser.getId());
    }

    @Test
    public void testAddToBalance_UserNotAuthenticated() {
        UserAdditionsDTO dto = new UserAdditionsDTO();
        dto.setAmount(new BigDecimal("500.00"));
        dto.setDescription("Test Addition");

        when(userService.getAuthenticatedUser()).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> accountingService.addToBalance(dto));

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, never()).addToBalance(any(), anyLong());
        verify(userAdditionsLogDAO, never()).save(any(UserAdditionsLog.class));
        verify(userAccountingDAO, never()).findCurrentBalanceByUser_Id(anyLong());
    }

    @Test
    public void testGetBalance() {
        when(userAccountingDAO.findCurrentBalanceByUser_Id(testUser.getId())).thenReturn(new BigDecimal("3000.00"));

        BigDecimal balance = accountingService.getBalance();

        assertEquals(new BigDecimal("3000.00"), balance);

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, times(1)).findCurrentBalanceByUser_Id(testUser.getId());
    }

    @Test
    public void testGetSalary() {
        when(userAccountingDAO.findSalaryByUser_Id(testUser.getId())).thenReturn(new BigDecimal("5000.00"));

        BigDecimal salary = accountingService.getSalary();

        assertEquals(new BigDecimal("5000.00"), salary);

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, times(1)).findSalaryByUser_Id(testUser.getId());
    }

    @Test
    public void testGetMonthlySpentPercent() {
        when(userAccountingDAO.findSalaryByUser_Id(testUser.getId())).thenReturn(new BigDecimal("5000.00"));
        when(userAccountingDAO.findCurrentBalanceByUser_Id(testUser.getId())).thenReturn(new BigDecimal("1000.00"));

        BigDecimal spentPercent = accountingService.getMonthlySpentPercent();

        assertEquals(new BigDecimal("0.80"), spentPercent); // (5000 - 1000) / 5000 = 0.8

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, times(1)).findSalaryByUser_Id(testUser.getId());
        verify(userAccountingDAO, times(1)).findCurrentBalanceByUser_Id(testUser.getId());
    }

    @Test
    public void testGetMonthlySpentPercent_ZeroSalary() {
        when(userAccountingDAO.findSalaryByUser_Id(testUser.getId())).thenReturn(BigDecimal.ZERO);
        when(userAccountingDAO.findCurrentBalanceByUser_Id(testUser.getId())).thenReturn(new BigDecimal("1000.00"));

        BigDecimal spentPercent = accountingService.getMonthlySpentPercent();

        assertEquals(BigDecimal.ZERO, spentPercent);

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, times(1)).findSalaryByUser_Id(testUser.getId());
        verify(userAccountingDAO, times(1)).findCurrentBalanceByUser_Id(testUser.getId());
    }

    @Test
    public void testUpdateSalaryAmount() {
        BigDecimal newSalary = new BigDecimal("6000.00");

        accountingService.updateSalaryAmount(newSalary);

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, times(1)).updateUserSalary(newSalary, testUser.getId());
    }

    @Test
    public void testUpdateSalaryAmount_UserNotAuthenticated() {
        BigDecimal newSalary = new BigDecimal("6000.00");
        when(userService.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UserException.UserNotAuthenticatedException.class, () -> accountingService.updateSalaryAmount(newSalary));

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, never()).updateUserSalary(any(), anyLong());
    }

    @Test
    public void testUpdateSalaryDate() {
        BigDecimal newSalaryDate = new BigDecimal("15");

        accountingService.updateSalaryDate(newSalaryDate);

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, times(1)).updateUserSalaryDate(newSalaryDate, testUser.getId());
    }

    @Test
    public void testUpdateSalaryDate_UserNotAuthenticated() {
        BigDecimal newSalaryDate = new BigDecimal("15");
        when(userService.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UserException.UserNotAuthenticatedException.class, () -> accountingService.updateSalaryDate(newSalaryDate));

        verify(userService, times(1)).getAuthenticatedUser();
        verify(userAccountingDAO, never()).updateUserSalaryDate(any(), anyLong());
    }
}
