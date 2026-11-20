package com.shop.onlineshopping.repository;

import com.shop.onlineshopping.dto.CustomerOrderStats;
import com.shop.onlineshopping.entity.Order;
import com.shop.onlineshopping.entity.OrderStatus;
import com.shop.onlineshopping.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    Optional<Order> findByStripeSessionId(String stripeSessionId);
    long countByStatusAndOrderDateGreaterThanEqual(OrderStatus status, Instant since);

    /**
     * Per-customer PAID-order totals, computed in one grouped query. Only PAID orders count
     * (a pending/cancelled order isn't a real purchase). {@code COALESCE(..,0)} keeps the sum
     * from ever being null. Customers with no paid orders simply don't appear in this list —
     * the service treats a missing row as "0 orders, 0 spent".
     */
    @Query("""
            SELECT new com.shop.onlineshopping.dto.CustomerOrderStats(
                       o.user.id, COUNT(o), COALESCE(SUM(o.totalAmount), 0))
            FROM Order o
            WHERE o.status = com.shop.onlineshopping.entity.OrderStatus.PAID
            GROUP BY o.user.id
            """)
    List<CustomerOrderStats> paidOrderStatsByUser();
}
