package com.github.rafaelmelo23.expense_tracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "expenses")
public class Expenses {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 512)
    private String name;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "description", length = 350)
    private String description;

    @Column(name = "is_recurrent")
    private Boolean isRecurrent = false;

}