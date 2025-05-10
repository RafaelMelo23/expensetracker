package com.github.rafaelmelo23.expense_tracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "user_accounting")
public class UserAccounting {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "salaryDate", nullable = false)
    private int salaryDate;

    @Column(name = "monthly_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlySalary;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "local_user_id", unique = true)
    private LocalUser user;

    public void setId(Long id) {
        this.id = id;
    }

    public void setSalaryDate(int salaryDate) {
        this.salaryDate = salaryDate;
    }

    public void setMonthlySalary(BigDecimal monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public void setUser(LocalUser user) {
        this.user = user;
    }
}