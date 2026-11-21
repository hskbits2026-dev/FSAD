package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.AddressResponse;
import com.shop.onlineshopping.dto.CustomerOrderStats;
import com.shop.onlineshopping.dto.CustomerSummaryResponse;
import com.shop.onlineshopping.entity.Address;
import com.shop.onlineshopping.entity.Role;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.repository.AddressRepository;
import com.shop.onlineshopping.repository.OrderRepository;
import com.shop.onlineshopping.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business logic for the admin Customers tab. A {@code @Service} is just a class that holds
 * rules/logic and sits between the controller (web layer) and the repositories (database layer).
 *
 * <p>Its job here: list every CUSTOMER with their contact details, saved addresses, how many paid
 * orders they've placed and how much they've spent. To avoid running extra queries per customer
 * (the "N+1" problem), order totals AND addresses are each fetched in ONE query and matched to
 * each customer in memory.
 */
@Service
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;

    public AdminCustomerService(UserRepository userRepository, OrderRepository orderRepository,
                                AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> listCustomers() {
        List<User> customers = userRepository.findByRoleOrderByCreatedAtAsc(Role.CUSTOMER);

        // One grouped query → a quick lookup table keyed by user id.
        Map<Long, CustomerOrderStats> statsByUser = orderRepository.paidOrderStatsByUser().stream()
                .collect(Collectors.toMap(CustomerOrderStats::userId, Function.identity()));

        // One query for ALL their addresses, grouped by owner id (default-first, from the query).
        Map<Long, List<Address>> addressesByUser = addressRepository
                .findByUserInOrderByIsDefaultDescIdAsc(customers).stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        return customers.stream()
                .map(user -> toSummary(user, statsByUser.get(user.getId()),
                        addressesByUser.getOrDefault(user.getId(), List.of())))
                .toList();
    }

    /** Combine a customer with their stats row (null when no paid orders) and their addresses. */
    private CustomerSummaryResponse toSummary(User user, CustomerOrderStats stats, List<Address> addresses) {
        long orderCount = stats != null ? stats.orderCount() : 0L;
        BigDecimal totalSpent = stats != null ? stats.totalSpent() : BigDecimal.ZERO;
        List<AddressResponse> addressDtos = new ArrayList<>(addresses.stream().map(AddressResponse::from).toList());
        return CustomerSummaryResponse.from(user, addressDtos, orderCount, totalSpent);
    }
}
