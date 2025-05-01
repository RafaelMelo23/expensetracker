package com.github.rafaelmelo23.expense_tracker.dto.auth;

import com.github.rafaelmelo23.expense_tracker.dto.expense.ExpenseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FirstRegistryDTO {

    @NotNull
    private List<ExpenseDTO> expenses;
    @NotNull
    private int salaryDate;
    @NotNull
    private BigDecimal monthlySalary;
    @NotNull
    private BigDecimal currentBalance;

}
