package com.github.rafaelmelo23.expense_tracker.api.expense;

import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseByMonthDTO;
import com.github.rafaelmelo23.expense_tracker.dto.auth.FirstRegistryDTO;
import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseDTO;
import com.github.rafaelmelo23.expense_tracker.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/first/registry")
    public ResponseEntity<?> createExpense(@RequestBody FirstRegistryDTO registryDTO) {

        expenseService.firstRegistry(registryDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<BigDecimal> registerExpense(@RequestBody ExpenseDTO expenseDTO) {
        return ResponseEntity.ok().body(expenseService.persistExpense(expenseDTO));
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        return ResponseEntity.ok().body(expenseService.getAllExpenses());
    }

    @GetMapping("/get/all/v2")
    public ResponseEntity<ExpenseByMonthDTO> getAllExpensesByMonth() {
        return ResponseEntity.ok().body(expenseService.getYearlyExpensesByMonth());
    }

}
