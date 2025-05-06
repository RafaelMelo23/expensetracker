package com.github.rafaelmelo23.expense_tracker.dto.expense;

import com.github.rafaelmelo23.expense_tracker.model.Expense;
import com.github.rafaelmelo23.expense_tracker.model.enums.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseDTO {

    @NotBlank
    private LocalDateTime expenseDate;
    @NotNull
    private Boolean isRecurrent;
    @NotNull
    private BigDecimal expenseAmount;
    private String expenseName;
    private ExpenseCategory expenseCategory;
    private String description;

    public static ExpenseDTO toDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setExpenseName(expense.getName());
        dto.setExpenseCategory(expense.getCategory() != null ? expense.getCategory() : ExpenseCategory.OTHER);
        dto.setExpenseAmount(expense.getAmount());
        dto.setExpenseDate(expense.getDate());
        dto.setDescription(expense.getDescription());
        dto.setIsRecurrent(expense.getIsRecurrent());
        return dto;

    }
}
