package com.github.rafaelmelo23.expense_tracker.model.dao;

import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LocalUserDAO extends ListCrudRepository<LocalUser, Long> {

    Optional<LocalUser> findByEmailIgnoreCase(String email);

    @Query("SELECT u.isFirstLogin FROM LocalUser u WHERE u.id = :userId")
    boolean checkIsUserFirstLogin(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE LocalUser u SET u.isFirstLogin = false WHERE u.id = :userId")
    void setUserFirstLoginToFalse(@Param("userId") Long userId);
}
