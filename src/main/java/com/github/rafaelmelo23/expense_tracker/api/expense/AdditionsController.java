package com.github.rafaelmelo23.expense_tracker.api.expense;

import com.github.rafaelmelo23.expense_tracker.dto.expense.UserAdditionsDTO;
import com.github.rafaelmelo23.expense_tracker.service.AccountingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/additions")
public class AdditionsController {

    private final AccountingService accountingService;

    public AdditionsController(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    @GetMapping("/get/yearly")
    public ResponseEntity<List<UserAdditionsDTO>> getYearlyAdditions(int year) {

        return ResponseEntity.ok().body(accountingService.getAllYearAdditions(year));
    }

    @PostMapping("/add/balance")
    public ResponseEntity<BigDecimal> addToBalance(@RequestBody UserAdditionsDTO dto) throws AccessDeniedException {

        return ResponseEntity.ok().body(accountingService.addToBalance(dto));
    }

    @PutMapping("/salary/update")
    public ResponseEntity<Void> updateSalaryAmount(@RequestParam BigDecimal salaryAmount) {
        accountingService.updateSalaryAmount(salaryAmount);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/salary/date/update")
    public ResponseEntity<Void> updateSalaryDate(@RequestParam BigDecimal salaryDate) {
        accountingService.updateSalaryDate(salaryDate);
        return ResponseEntity.ok().build();
    }
}
