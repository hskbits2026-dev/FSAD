package com.shop.onlineshopping.entity;

/**
 * Whether a customer account is live or has been retired.
 *
 * <p>We use a soft-delete model: instead of physically deleting a customer row (which would
 * also orphan their orders and feedback), we flip the status to {@link #INACTIVE}. The row —
 * and all its history — stays in the database; it's just hidden/greyed in the admin UI.
 *
 * <p>Stored as a VARCHAR, not a native MySQL ENUM: {@code @Enumerated(EnumType.STRING)} on the
 * {@code User.status} field writes the name ("ACTIVE"/"INACTIVE") as text. That keeps schema
 * changes painless — adding a new status later is a Java-only change, no {@code ALTER TABLE}.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE
}
