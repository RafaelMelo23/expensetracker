package com.github.rafaelmelo23.expense_tracker.api.controller.expense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseDTO;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.enums.ExpenseCategory;
import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import com.github.rafaelmelo23.expense_tracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseService expenseService;

    private LocalUser testUser;

    @BeforeEach
    public void setUp() {
        testUser = new LocalUser();
        testUser.setId(1001L);
        testUser.setRole(Role.ROLE_USER);
        testUser.setEmail("anakin@example.com");
        testUser.setIsFirstLogin(false);

        setupSecurityContext(testUser);
    }

    private void setupSecurityContext(LocalUser user) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testCreateExpense_FirstRegistry() throws Exception {

        testUser.setIsFirstLogin(true);
        String registryDTO = "{\"expenses\": [{\"expenseAmount\": 100.00,\"expenseCategory\": \"FOOD\",\"expenseDate\": \"2024-08-01T10:00:00\",\"expenseName\": \"Groceries\",\"isRecurrent\": false,\"description\": \"Weekly shopping\"},{\"expenseAmount\": 50.00,\"expenseCategory\": \"TRANSPORT\",\"expenseDate\": \"2024-08-03T18:30:00\",\"expenseName\": \"Gas\",\"isRecurrent\": true,\"description\": \"Fuel for the car\"}],\"salaryDate\": 5,\"monthlySalary\": 5000.00,\"currentBalance\": 4850.00}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/expense/first/registry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registryDTO))
                .andExpect(status().isOk());
    }

    @Test
    public void testRegisterExpense_WithCategory() throws Exception {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setExpenseAmount(new BigDecimal("100.00"));
        expenseDTO.setDescription("New Expense");
        expenseDTO.setExpenseCategory(ExpenseCategory.OTHER);
        expenseDTO.setExpenseDate(LocalDateTime.now());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/expense/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void testRegisterExpense_WithoutCategory() throws Exception {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setExpenseAmount(new BigDecimal("70.00"));
        expenseDTO.setDescription("Another Expense");
        expenseDTO.setExpenseDate(LocalDateTime.now());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/expense/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllExpenses() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/expense/get/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].expenseAmount").value(300.00))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Weekly grocery shopping"));
    }

    @Test
    public void testGetAllExpensesByMonth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/expense/get/all/v2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.monthlyExpenses.MAY[0].expenseAmount").value(300));
    }
}
