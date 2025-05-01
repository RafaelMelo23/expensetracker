package com.github.rafaelmelo23.expense_tracker.api.controller.authentication;


import com.github.rafaelmelo23.expense_tracker.dto.auth.LoginBody;
import com.github.rafaelmelo23.expense_tracker.dto.auth.RegistrationBody;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserServiceController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<LocalUser> registerUser(@RequestBody @Valid RegistrationBody registrationBody) {

        userService.registerUser(registrationBody);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginBody loginBody, HttpServletResponse response) {

        String jwt = userService.loginUser(loginBody.getEmail(), loginBody.getPassword());

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Cookie jwtCookie = new Cookie("JWT", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(604800);
        response.addCookie(jwtCookie);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
