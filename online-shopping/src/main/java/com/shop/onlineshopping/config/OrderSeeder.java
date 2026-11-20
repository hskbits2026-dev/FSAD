package com.shop.onlineshopping.config;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Seeds realistic PAID orders so the admin Reports/Sales screens have something to show.
 * Without this, every sales query returns empty because they all filter {@code status = PAID}
 * and the orders table is empty.
 *
 * <p><b>Reseedable:</b> runs only when the {@code orders} table is empty. To regenerate, wipe
 * {@code order_items} then {@code orders} (child before parent) and restart the app.
 *
 * <p><b>Spread over a year on purpose:</b> the Reports screen has Week/Month/Quarter/Year
 * filters, each of which only counts orders newer than that cutoff. So we scatter order dates
 * across the whole year — a few in the last week, more in the last month, and so on — so that
 * every filter shows data, and newer windows are a realistic subset of older ones.
 *
 * <p><b>Does NOT change product stock (QOH).</b> The real payment flow decrements stock, but
 * this seeder is purely demo sales history: keeping it additive to just {@code orders} +
 * {@code order_items} makes it clean to wipe and reseed, and leaves the catalogue untouched.
 *
 * <p>Runs LAST (see {@code @Order(4)}): products and customers must exist first.
 */
@Component
@Profile("!test") // demo sales history — never in the test profile (would collide with test fixtures)
@org.springframework.core.annotation.Order(4) // run LAST: needs products + customers to exist
public class OrderSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OrderSeeder.class);

    /** Fixed seed → the same demo orders every time we reseed (stable for screenshots/demos). */
    private final Random random = new Random(42);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderSeeder(OrderRepository orderRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return; // Orders already present — don't duplicate on restart.
        }
        List<Product> products = productRepository.findAll();
        List<User> customers = activeCustomers();
        if (products.isEmpty() || customers.isEmpty()) {
            log.warn("Skipping order seeding (products={}, active customers={}).",
                    products.size(), customers.size());
            return;
        }

        // Scatter orders across time windows: {count, oldestDaysAgo, newestDaysAgo}.
        int created = 0;
        created += seedBucket(6, 0, 7, customers, products);     // last week
        created += seedBucket(10, 8, 30, customers, products);   // rest of last month
        created += seedBucket(12, 31, 90, customers, products);  // rest of last quarter
        created += seedBucket(14, 91, 360, customers, products); // rest of last year
        log.info("Seeded {} demo PAID orders across {} customers.", created, customers.size());
    }

    /** Only ACTIVE customers get orders (ignore the soft-deleted test accounts and the admin). */
    private List<User> activeCustomers() {
        return userRepository.findByRoleOrderByCreatedAtAsc(Role.CUSTOMER).stream()
                .filter(u -> u.getStatus() != UserStatus.INACTIVE) // null (legacy) counts as active
                .toList();
    }

    /** Create {@code count} orders whose dates fall between {@code newestDaysAgo}..{@code oldestDaysAgo}. */
    private int seedBucket(int count, int newestDaysAgo, int oldestDaysAgo,
                           List<User> customers, List<Product> products) {
        for (int i = 0; i < count; i++) {
            int daysAgo = newestDaysAgo + random.nextInt(Math.max(1, oldestDaysAgo - newestDaysAgo + 1));
            Instant when = Instant.now()
                    .minus(daysAgo, ChronoUnit.DAYS)
                    .minus(random.nextInt(24), ChronoUnit.HOURS);
            createOrder(pick(customers), when, products);
        }
        return count;
    }

    /** Build one PAID order with 1–4 distinct products, 1–3 units each, and save it (items cascade). */
    private void createOrder(User customer, Instant when, List<Product> products) {
        Order order = new Order();
        order.setUser(customer);
        order.setStatus(OrderStatus.PAID);
        order.setOrderDate(when);
        order.setStripeSessionId("seed_" + Math.abs(random.nextLong()));

        BigDecimal total = BigDecimal.ZERO;
        for (Product product : pickDistinctProducts(products, 1 + random.nextInt(4))) {
            int qty = 1 + random.nextInt(3);
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setPriceAtPurchase(product.getPrice()); // price "frozen" at purchase time
            order.getItems().add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
        order.setTotalAmount(total);
        orderRepository.save(order); // cascade = ALL saves the items too
    }

    /** Pick up to {@code n} distinct products by shuffling a copy of the catalogue. */
    private List<Product> pickDistinctProducts(List<Product> products, int n) {
        List<Product> copy = new ArrayList<>(products);
        Collections.shuffle(copy, random);
        return copy.subList(0, Math.min(n, copy.size()));
    }

    private <T> T pick(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}
