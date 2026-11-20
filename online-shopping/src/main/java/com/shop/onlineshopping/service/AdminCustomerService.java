package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.CustomerOrderStats;
import com.shop.onlineshopping.dto.CustomerSummaryResponse;
import com.shop.onlineshopping.entity.Role;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.repository.OrderRepository;
import com.shop.onlineshopping.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business logic for the admin Customers tab. A {@code @Service} is just a class that holds
 * rules/logic and sits between the controller (web layer) and the repositories (database layer).
 *
 * <p>Its one job here: list every CUSTOMER with how many paid orders they've placed and how much
 * they've spent. To avoid running a count query per customer (the "N+1" problem), it fetches all
 * the order totals in ONE grouped query and then matches each customer to their row in memory.
 */
@Service
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminCustomerService(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> listCustomers() {
        // One grouped query → a quick lookup table keyed by user id.
        Map<Long, CustomerOrderStats> statsByUser = orderRepository.paidOrderStatsByUser().stream()
                .collect(Collectors.toMap(CustomerOrderStats::userId, Function.identity()));

        return userRepository.findByRoleOrderByCreatedAtAsc(Role.CUSTOMER).stream()
                .map(user -> toSummary(user, statsByUser.get(user.getId())))
                .toList();
    }

    /** Combine a customer with their stats row (which is null when they have no paid orders). */
    private CustomerSummaryResponse toSummary(User user, CustomerOrderStats stats) {
        long orderCount = stats != null ? stats.orderCount() : 0L;
        BigDecimal totalSpent = stats != null ? stats.totalSpent() : BigDecimal.ZERO;
        return CustomerSummaryResponse.from(user, orderCount, totalSpent);
    }
}
