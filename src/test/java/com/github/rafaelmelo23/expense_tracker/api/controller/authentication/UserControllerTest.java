package com.github.rafaelmelo23.expense_tracker.api.controller.authentication;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rafaelmelo23.expense_tracker.dto.auth.LoginBody;
import com.github.rafaelmelo23.expense_tracker.dto.auth.RegistrationBody;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long ANAKIN_ID = 1001L;
    private static final String ANAKIN_EMAIL = "anakin@example.com";
    private static final String ANAKIN_FIRST_NAME = "Anakin";
    private static final String ANAKIN_LAST_NAME = "Skywalker";
    private static final boolean ANAKIN_IS_FIRST_LOGIN = true;
    private static final BigDecimal ANAKIN_SALARY = new BigDecimal("4500.00");
    private static final BigDecimal ANAKIN_BALANCE = new BigDecimal("2250.00");

    private static final BigDecimal ANAKIN_SALARY_SPENT_PERCENTAGE = new BigDecimal("0.5"); // 50%

    private static final String COMMON_PLAIN_TEXT_PASSWORD = "Password1";

    private LocalUser anakinUserPrincipal;

    @BeforeEach
    public void setUp() {

        anakinUserPrincipal = new LocalUser();
        anakinUserPrincipal.setId(ANAKIN_ID);
        anakinUserPrincipal.setEmail(ANAKIN_EMAIL);
        anakinUserPrincipal.setFirstName(ANAKIN_FIRST_NAME);
        anakinUserPrincipal.setLastName(ANAKIN_LAST_NAME);
        anakinUserPrincipal.setRole(Role.ROLE_USER);
        anakinUserPrincipal.setIsFirstLogin(ANAKIN_IS_FIRST_LOGIN);

    }

    private void setupSecurityContext(LocalUser principal) {
        if (principal == null) {
            SecurityContextHolder.clearContext();
            return;
        }

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(principal.getRole().name()));

        UserDetails userDetails = new User(principal.getEmail(), "", authorities);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                userDetails.getAuthorities()
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    public void testRegisterUser_Success() throws Exception {
        RegistrationBody registrationBody = new RegistrationBody();
        String uniqueEmail = "newuser-" + UUID.randomUUID().toString() + "@example.com";
        registrationBody.setEmail(uniqueEmail);
        registrationBody.setPassword("newValidPassword123");
        registrationBody.setFirstName("New");
        registrationBody.setLastName("RegisteredUser");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationBody)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testRegisterUser_ValidationFailure() throws Exception {
        RegistrationBody registrationBody = new RegistrationBody();
        registrationBody.setEmail("notanemail");
        registrationBody.setPassword("short");
        registrationBody.setFirstName("i"); // invalid
        registrationBody.setLastName("n");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationBody)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testLoginUser_Success_Anakin() throws Exception {
        LoginBody loginBody = new LoginBody();
        loginBody.setEmail(ANAKIN_EMAIL);
        loginBody.setPassword(COMMON_PLAIN_TEXT_PASSWORD);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").exists())
                .andExpect(jsonPath("$.email", is(ANAKIN_EMAIL)))
                .andReturn();

        Cookie jwtCookie = result.getResponse().getCookie("JWT");
        assertNotNull(jwtCookie, "JWT cookie should not be null");
        assertEquals("JWT", jwtCookie.getName());
        assertNotNull(jwtCookie.getValue(), "JWT cookie value should not be null");
        assertTrue(jwtCookie.isHttpOnly(), "JWT cookie should be HttpOnly");
        assertTrue(jwtCookie.getSecure(), "JWT cookie should be Secure");
        assertEquals("/", jwtCookie.getPath());
        assertEquals(1209600, jwtCookie.getMaxAge());
    }

    @Test
    public void testLoginUser_Unauthorized_WrongPassword() throws Exception {
        LoginBody loginBody = new LoginBody();
        loginBody.setEmail(ANAKIN_EMAIL); // Existing user from data.sql
        loginBody.setPassword("wrongpassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLoginUser_Unauthorized_UserNotFound() throws Exception {
        LoginBody loginBody = new LoginBody();
        loginBody.setEmail("nonexistentuser-" + UUID.randomUUID() + "@example.com");
        loginBody.setPassword("anypassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetBalance_Success_Anakin() throws Exception {
        setupSecurityContext(anakinUserPrincipal);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/get/balance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(ANAKIN_BALANCE.doubleValue())); // Compare doubleValue for BigDecimal
    }

    @Test
    public void testGetBalance_Unauthorized() throws Exception {
        setupSecurityContext(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/get/balance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetSalary_Success_Anakin() throws Exception {
        setupSecurityContext(anakinUserPrincipal);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/get/salary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(ANAKIN_SALARY.doubleValue()));
    }

    @Test
    public void testGetSalary_Unauthorized() throws Exception {
        setupSecurityContext(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/get/salary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetSalarySpentPercentage_Success_Anakin() throws Exception {
        setupSecurityContext(anakinUserPrincipal);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/get/salary/spent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(ANAKIN_SALARY_SPENT_PERCENTAGE.doubleValue()));
    }

    @Test
    public void testGetSalarySpentPercentage_Unauthorized() throws Exception {
        setupSecurityContext(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/get/salary/spent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}