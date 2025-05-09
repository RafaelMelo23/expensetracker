package com.github.rafaelmelo23.expense_tracker.config;

import com.github.rafaelmelo23.expense_tracker.model.dao.UserAccountingDAO;
import com.github.rafaelmelo23.expense_tracker.service.AccountingService;
import com.github.rafaelmelo23.expense_tracker.service.UserService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@org.springframework.boot.test.context.TestConfiguration
@Profile("test")
public class TestConfiguration {

    @Bean
    @Primary
    public AccountingService accountingService() {
        return Mockito.mock(AccountingService.class);
    }

    @Bean
    @Primary
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    @Primary
    public UserAccountingDAO userAccountingDAO() {
        return Mockito.mock(UserAccountingDAO.class);
    }

}