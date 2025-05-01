package com.github.rafaelmelo23.expense_tracker.dao;

import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface LocalUserDAO extends ListCrudRepository<LocalUser, Long> {

    Optional<LocalUser> findByEmailIgnoreCase(String email);
}
