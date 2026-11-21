package com.shop.onlineshopping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    /**
     * Customer's phone number. Deliberately NOT {@code nullable = false}: like {@code status},
     * this column is added to a table that already has rows, so a NOT NULL add could fail. New
     * profiles set it via Edit Profile; old rows start NULL and are backfilled by CustomerSeeder.
     */
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Soft-delete flag. Deliberately NOT {@code nullable = false}: this column is being added
     * to a table that already has rows, and a NOT NULL add could fail or junk-fill those old
     * rows. New accounts always get a value (default below); old rows start as NULL and are
     * backfilled to ACTIVE by CustomerSeeder on the next startup. Anything reading this should
     * treat NULL as ACTIVE.
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
