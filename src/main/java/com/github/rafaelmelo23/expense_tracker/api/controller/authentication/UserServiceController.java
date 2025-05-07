package com.github.rafaelmelo23.expense_tracker.api.controller.authentication;


import com.github.rafaelmelo23.expense_tracker.dto.auth.LoginBody;
import com.github.rafaelmelo23.expense_tracker.dto.auth.RegistrationBody;
import com.github.rafaelmelo23.expense_tracker.dto.auth.UserDTO;
import com.github.rafaelmelo23.expense_tracker.dto.expense.UserAdditionsDTO;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.service.AccountingService;
import com.github.rafaelmelo23.expense_tracker.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserServiceController {

    private final UserService userService;
    private final AccountingService accountingService;

    @PostMapping("/register")
    public ResponseEntity<LocalUser> registerUser(@RequestBody @Valid RegistrationBody registrationBody) {

        userService.registerUser(registrationBody);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginUser(@RequestBody LoginBody loginBody, HttpServletResponse response) {

        UserDTO dto = userService.loginUser(loginBody.getEmail(), loginBody.getPassword());

        if (dto.getJwtToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Cookie jwtCookie = new Cookie("JWT", dto.getJwtToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(1209600);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/get/balance")
    public ResponseEntity<BigDecimal> getBalance() {

        return ResponseEntity.ok().body(accountingService.getBalance());
    }

    @GetMapping("/get/salary")
    public ResponseEntity<BigDecimal> getSalary() {

        return ResponseEntity.ok().body(accountingService.getSalary());
    }

    @GetMapping("/get/salary/spent")
    public ResponseEntity<BigDecimal> getSalarySpentPercentage() {

        return ResponseEntity.ok().body(accountingService.getMonthlySpentPercent());
    }
}
