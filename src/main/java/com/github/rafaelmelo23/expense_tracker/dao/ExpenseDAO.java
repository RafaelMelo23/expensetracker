package com.github.rafaelmelo23.expense_tracker.dao;

import com.github.rafaelmelo23.expense_tracker.model.Expense;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseDAO extends ListCrudRepository<Expense, Long> {

    /**
     * Retrieves relevant expenses for a given user and date range.
     *
     * The query includes:
     * <ul>
     *   <li>Non-recurrent expenses strictly within the specified date range.</li>
     *   <li>Recurrent expenses whose original date is before or within the end of the range,
     *       assuming they remain valid beyond their creation date.</li>
     * </ul>
     *
     * This logic supports dynamic expansion of recurrent expenses (e.g., monthly) in memory
     * after query execution if needed.
     *
     * This logic could be replaced with two simpler separated methods, but i chose to make it one query
     * for simpleness and performance.
     *
     * @param userId The ID of the user whose expenses are being retrieved.
     * @param start The start of the date range (inclusive).
     * @param end The end of the date range (inclusive).
     * @return A list of relevant Expense entities.
     */

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND " +
            "((e.isRecurrent = false AND e.date BETWEEN :start AND :end) OR " +
            " (e.isRecurrent = true AND e.date <= :end))")
    List<Expense> findRelevantExpenses(@Param("userId") Long userId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT ex FROM Expense ex WHERE ex.user.id = :userId AND ex.isRecurrent = true")
    List<BigDecimal> findRecurrentExpensesByUser(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserAccounting u SET u.currentBalance =:newBalance WHERE u.user.id = :userId")
    void monthlyBalanceUpdate(@Param("userId") Long userId, @Param("newBalance") BigDecimal newBalance);
}
