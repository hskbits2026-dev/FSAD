package com.shop.onlineshopping.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A single delivery address belonging to one customer. A customer can have many of these
 * (see the {@code @ManyToOne} back to {@link User}); exactly one of them is their default,
 * flagged by {@link #isDefault}. The "one default" rule is enforced in the service layer
 * (AddressService), not here — the entity just stores the flag.
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The owner of this address. Child side owns the FK, mirroring OrderItem/CartItem. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Short nickname shown in pickers, e.g. "Home" or "Work". */
    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String line1;

    /** Optional second address line (flat/landmark). */
    private String line2;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    /** True for the customer's default address. Only one per customer (enforced in the service). */
    @Column(nullable = false)
    private boolean isDefault = false;
}
