package com.github.rafaelmelo23.expense_tracker.api.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlPagesMapping {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/login")
    public String teste() {
        return "login";
    }

}
