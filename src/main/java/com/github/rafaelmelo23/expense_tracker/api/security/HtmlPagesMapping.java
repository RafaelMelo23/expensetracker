package com.github.rafaelmelo23.expense_tracker.api.security;

import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlPagesMapping {

    private final LocalUserDAO localUserDAO;
    private final UserService userService;

    public HtmlPagesMapping(LocalUserDAO localUserDAO, UserService userService) {
        this.localUserDAO = localUserDAO;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal LocalUser user) {

        if (user != null) {
            boolean isFirstLogin = localUserDAO.checkIsUserFirstLogin(user.getId());
            model.addAttribute("isFirstLogin", isFirstLogin);
        }

        if (user == null) {
            return "redirect:/login";
        }

        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/first/registry")
    public String firstRegistry() {
        return "first-registry";
    }

    @GetMapping("/calendar")
    public String calendario() {
        return "calendar";
    }

}
