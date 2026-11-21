package com.shop.onlineshopping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Links one customer to one product they've favourited (wishlist). It's a join row: a
 * {@code @ManyToOne} to the {@link User} and a {@code @ManyToOne} to the {@link Product}.
 * The {@code uniqueConstraint} stops the same product being favourited twice by one customer,
 * so the "add to favourites" action is naturally idempotent.
 */
@Entity
@Table(name = "favourites", uniqueConstraints =
        @UniqueConstraint(name = "uq_favourite_user_product", columnNames = {"user_id", "product_id"}))
@Getter
@Setter
public class Favourite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** When it was favourited — used to show newest-first in the wishlist. */
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
