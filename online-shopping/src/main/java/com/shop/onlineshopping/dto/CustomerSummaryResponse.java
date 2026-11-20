package com.shop.onlineshopping.dto;

import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.entity.UserStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * What the admin Customers tab sees for one customer. This is a safe, flattened view of a
 * {@link User}: notably it does NOT include the password hash. Order numbers are passed in
 * because they come from a separate aggregate query, not from the User row itself.
 *
 * @param id         customer id
 * @param fullName   display name
 * @param email      login email
 * @param status     ACTIVE or INACTIVE (null in the DB is reported as ACTIVE)
 * @param joinedAt   when the account was created
 * @param orderCount number of PAID orders
 * @param totalSpent lifetime spend across PAID orders
 */
public record CustomerSummaryResponse(
        Long id,
        String fullName,
        String email,
        UserStatus status,
        Instant joinedAt,
        long orderCount,
        BigDecimal totalSpent
) {
    /** Build the view from a user plus its pre-computed order stats (orderCount/totalSpent). */
    public static CustomerSummaryResponse from(User user, long orderCount, BigDecimal totalSpent) {
        UserStatus status = user.getStatus() != null ? user.getStatus() : UserStatus.ACTIVE;
        return new CustomerSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                status,
                user.getCreatedAt(),
                orderCount,
                totalSpent != null ? totalSpent : BigDecimal.ZERO);
    }
}
