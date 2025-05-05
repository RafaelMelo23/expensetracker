package com.github.rafaelmelo23.expense_tracker.dto.expense;


import lombok.Getter;
import lombok.Setter;

import java.time.Month;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExpenseByMonthDTO {

    private Map<Month, List<ExpenseDTO>> monthlyExpenses = new EnumMap<>(Month.class);

    public void addExpenses(Month month, List<ExpenseDTO> expenses) {
        this.monthlyExpenses.put(month, expenses);
    }
}
