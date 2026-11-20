package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.entity.Category;
import com.shop.onlineshopping.entity.Order;
import com.shop.onlineshopping.entity.OrderItem;
import com.shop.onlineshopping.entity.OrderStatus;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.Role;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.entity.UserStatus;
import com.shop.onlineshopping.repository.OrderRepository;
import com.shop.onlineshopping.repository.ProductRepository;
import com.shop.onlineshopping.repository.UserRepository;
import com.shop.onlineshopping.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCustomerControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired JwtService jwtService;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        adminToken = tokenFor(Role.ADMIN, "admin@shop.com");
        customerToken = tokenFor(Role.CUSTOMER, "cust@shop.com");
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String tokenFor(Role role, String email) {
        User u = new User();
        u.setEmail(email);
        u.setRole(role);
        return jwtService.generateToken(u);
    }

    private User persistCustomer(String name, String email, UserStatus status) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setRole(Role.CUSTOMER);
        u.setStatus(status);
        return userRepository.save(u);
    }

    private void seedPaidOrder(User buyer, String price, int qty) {
        Product p = new Product();
        p.setName("Thing");
        p.setDescription("desc");
        p.setPrice(new BigDecimal(price));
        p.setQoh(100);
        p.setCategory(Category.ELECTRONICS);
        productRepository.save(p);

        Order order = new Order();
        order.setUser(buyer);
        order.setStatus(OrderStatus.PAID);
        order.setStripeSessionId("cs_" + System.nanoTime());
        order.setOrderDate(Instant.now());
        order.setTotalAmount(new BigDecimal(price).multiply(BigDecimal.valueOf(qty)));
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(p);
        item.setQuantity(qty);
        item.setPriceAtPurchase(new BigDecimal(price));
        order.getItems().add(item);
        orderRepository.save(order);
    }

    @Test
    void listCustomers_asAdmin_returnsCustomersWithOrderStats() throws Exception {
        User active = persistCustomer("Active Annie", "annie@shop.com", UserStatus.ACTIVE);
        persistCustomer("Inactive Ivan", "ivan@shop.com", UserStatus.INACTIVE);
        seedPaidOrder(active, "250.00", 2); // 1 order, spent 500

        mockMvc.perform(get("/api/admin/customers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                // oldest-first ordering → Annie (created first) is index 0
                .andExpect(jsonPath("$[0].email").value("annie@shop.com"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].orderCount").value(1))
                .andExpect(jsonPath("$[0].totalSpent").value(500.00))
                .andExpect(jsonPath("$[1].email").value("ivan@shop.com"))
                .andExpect(jsonPath("$[1].status").value("INACTIVE"))
                .andExpect(jsonPath("$[1].orderCount").value(0))
                .andExpect(jsonPath("$[1].totalSpent").value(0));
    }

    @Test
    void listCustomers_excludesAdminsAndNeverLeaksPasswordHash() throws Exception {
        persistCustomer("Annie", "annie@shop.com", UserStatus.ACTIVE);
        // the admin from setUp's token isn't persisted; persist a real admin to be sure it's filtered
        User admin = new User();
        admin.setFullName("Boss");
        admin.setEmail("boss@shop.com");
        admin.setPasswordHash("hash");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        mockMvc.perform(get("/api/admin/customers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("annie@shop.com"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void listCustomers_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/customers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listCustomers_anonymous_returns4xx() throws Exception {
        mockMvc.perform(get("/api/admin/customers"))
                .andExpect(status().is4xxClientError());
    }
}
