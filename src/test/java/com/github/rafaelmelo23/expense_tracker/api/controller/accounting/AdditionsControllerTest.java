package com.github.rafaelmelo23.expense_tracker.api.controller.accounting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rafaelmelo23.expense_tracker.dto.expense.UserAdditionsDTO;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import com.github.rafaelmelo23.expense_tracker.service.AccountingService;
import com.github.rafaelmelo23.expense_tracker.service.UserService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdditionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountingService accountingService;  // Inject the actual service

    @Autowired
    private UserService userService;  // Inject the actual service

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
    public void testGetYearlyAdditions() throws Exception {
        int year = 2025;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/additions/get/yearly")
                        .param("year", String.valueOf(year))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].amount").value(500))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Bonus from freelance job"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1]").doesNotExist());
    }

    @Test
    public void testAddToBalance() throws Exception {
        UserAdditionsDTO dto = new UserAdditionsDTO();
        dto.setAmount(new BigDecimal("1500.00"));
        dto.setDescription("Test Addition");
        dto.setCreatedAt(LocalDateTime.now() );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/additions/add/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("1500.00"));

    }

    @Test
    public void testAddToBalance_AccessDeniedException() throws Exception {
        UserAdditionsDTO dto = new UserAdditionsDTO();
        dto.setAmount(new BigDecimal("500.00"));
        dto.setDescription("Test Addition");

        setupSecurityContext(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/additions/add/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateSalaryAmount() throws Exception {
        BigDecimal salaryAmount = new BigDecimal("6000.00");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/additions/salary/update")
                        .param("salaryAmount", salaryAmount.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void testUpdateSalaryDate() throws Exception {
        BigDecimal salaryDate = new BigDecimal("15");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/additions/salary/date/update")
                        .param("salaryDate", salaryDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
