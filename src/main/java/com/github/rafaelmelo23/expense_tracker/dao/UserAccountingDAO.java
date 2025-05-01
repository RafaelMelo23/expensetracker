package com.github.rafaelmelo23.expense_tracker.dao;

import com.github.rafaelmelo23.expense_tracker.model.UserAccounting;
import com.github.rafaelmelo23.expense_tracker.model.interfaces.UserSalaryInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAccountingDAO extends ListCrudRepository<UserAccounting, Long> {

    UserAccounting findByUser_Id(Long userId);

    @Query("SELECT ua.user.id AS userId, " +
            "ua.monthlySalary as monthlySalary" +
            " FROM UserAccounting ua " +
            "WHERE ua.salaryDate = :salaryDate")
    List<UserSalaryInfo> findUserSalariesByDayOfTheMonth(@Param("salaryDate") int salaryDate);
}
