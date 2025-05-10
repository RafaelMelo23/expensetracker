package com.github.rafaelmelo23.expense_tracker.model;

import com.github.rafaelmelo23.expense_tracker.model.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "local_user")
public class LocalUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password", nullable = false, length = 72)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.ROLE_USER;

    @Column(name = "is_first_login")
    private Boolean isFirstLogin = true;

}