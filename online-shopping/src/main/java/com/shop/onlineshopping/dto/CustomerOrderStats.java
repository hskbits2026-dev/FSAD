package com.shop.onlineshopping.dto;

import java.math.BigDecimal;

/**
 * One row of "how much has this customer bought" — produced by a single grouped SQL query
 * (see {@code OrderRepository.paidOrderStatsByUser}) so the Customers table doesn't have to
 * run a separate count query per customer.
 *
 * @param userId     the customer's id
 * @param orderCount how many PAID orders they have
 * @param totalSpent sum of those orders' totals (never null — the query coalesces to 0)
 */
public record CustomerOrderStats(Long userId, long orderCount, BigDecimal totalSpent) {
}
