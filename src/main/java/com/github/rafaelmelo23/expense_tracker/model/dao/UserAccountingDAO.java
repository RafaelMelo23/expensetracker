package com.github.rafaelmelo23.expense_tracker.model.dao;

import com.github.rafaelmelo23.expense_tracker.model.UserAccounting;
import com.github.rafaelmelo23.expense_tracker.model.interfaces.UserSalaryInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserAccountingDAO extends ListCrudRepository<UserAccounting, Long> {

    Optional<UserAccounting> findByUser_Id(Long userId);

    @Query("SELECT ua.user.id AS userId, " +
            "ua.monthlySalary as monthlySalary" +
            " FROM UserAccounting ua " +
            "WHERE ua.salaryDate = :salaryDate")
    List<UserSalaryInfo> findUserSalariesByDayOfTheMonth(@Param("salaryDate") int salaryDate);

    @Modifying
    @Transactional
    @Query("UPDATE UserAccounting ua SET ua.monthlySalary = :salaryAmount WHERE ua.user.id = :userId")
    void updateUserSalary(@Param("salaryAmount") BigDecimal salaryAmount, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserAccounting ua SET ua.salaryDate = :salaryDate WHERE ua.user.id = :userId")
    void updateUserSalaryDate(@Param("salaryDate") BigDecimal salaryDate, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserAccounting ua SET ua.currentBalance = ua.currentBalance + :amount WHERE ua.user.id = :userId")
    void addToBalance(@Param("amount") BigDecimal amount, @Param("userId") Long userId);

    @Query("SELECT ua.currentBalance FROM UserAccounting ua WHERE ua.user.id = :userId")
    BigDecimal findCurrentBalanceByUser_Id(@Param("userId") Long userId);

    @Query("SELECT ua.monthlySalary FROM UserAccounting ua WHERE ua.user.id = :userId")
    BigDecimal findSalaryByUser_Id(Long userId);


}
