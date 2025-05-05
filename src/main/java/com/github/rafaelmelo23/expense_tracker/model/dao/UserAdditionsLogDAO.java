package com.github.rafaelmelo23.expense_tracker.model.dao;

import com.github.rafaelmelo23.expense_tracker.model.UserAdditionsLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserAdditionsLogDAO extends ListCrudRepository<UserAdditionsLog, Long> {

    @Query("SELECT ual FROM UserAdditionsLog ual WHERE ual.createdAt BETWEEN :startDate AND :endDate AND ual.user.id = :userId")
    List<UserAdditionsLog> findByCreatedAtBetweenAndUserId(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("userId") Long userId
    );

}
