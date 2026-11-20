package com.shop.onlineshopping.config;

import com.shop.onlineshopping.entity.Feedback;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.Role;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.entity.UserStatus;
import com.shop.onlineshopping.repository.FeedbackRepository;
import com.shop.onlineshopping.repository.ProductRepository;
import com.shop.onlineshopping.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Seeds a realistic mix of customer feedback so the admin dashboard's feedback section looks
 * meaningful: some general STORE feedback and some PRODUCT reviews, across a spread of ratings
 * (not just 5★). Without this the section was thin and repetitive.
 *
 * <p><b>Reseedable:</b> runs only when the {@code feedback} table is empty. To regenerate, wipe
 * the {@code feedback} table and restart.
 *
 * <p>Runs LAST (see {@code @Order(5)}): it needs customers and products to attach feedback to.
 * Excluded from the test profile ({@code @Profile("!test")}) so tests manage their own data.
 */
@Component
@Profile("!test")
@Order(5)
public class FeedbackSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FeedbackSeeder.class);

    /** General store feedback: {rating, message}. Deliberately spread across 2–5★. */
    private static final String[][] STORE_FEEDBACK = {
            {"5", "Checkout was smooth and delivery was quick. Will shop here again!"},
            {"5", "Love the new catalogue — great range across every category."},
            {"4", "Good experience overall, though I'd like a few more payment options."},
            {"4", "Clean, easy-to-use store. Found what I needed in minutes."},
            {"3", "Decent, but the order tracking could be a little clearer."},
            {"5", "Support replied fast when I had a question about my order."},
            {"2", "Site felt a bit slow during checkout on my phone."},
            {"5", "Lovely packaging and the prices are fair. Very happy."},
    };

    /** Product reviews (attached to real products, round-robin): {rating, message}. */
    private static final String[][] PRODUCT_REVIEWS = {
            {"5", "Exactly as described — excellent quality."},
            {"4", "Works well and good value for the price."},
            {"5", "Exceeded my expectations. Highly recommend!"},
            {"3", "It's okay — does the job but nothing special."},
            {"4", "Solid build, really happy with the purchase."},
            {"5", "Fantastic! Bought a second one as a gift."},
            {"2", "A little smaller than I expected from the photos."},
            {"4", "Good product and it arrived right on time."},
    };

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public FeedbackSeeder(FeedbackRepository feedbackRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (feedbackRepository.count() > 0) {
            return; // already has feedback — don't duplicate on restart
        }
        List<User> customers = activeCustomers();
        List<Product> products = productRepository.findAll();
        if (customers.isEmpty()) {
            log.warn("Skipping feedback seeding (no active customers).");
            return;
        }

        int created = 0;
        // Store feedback (no product attached).
        for (int i = 0; i < STORE_FEEDBACK.length; i++) {
            User author = customers.get(i % customers.size());
            save(author, null, Integer.parseInt(STORE_FEEDBACK[i][0]), STORE_FEEDBACK[i][1], i);
            created++;
        }
        // Product reviews (attached to products, round-robin) — only if there are products.
        for (int i = 0; i < PRODUCT_REVIEWS.length && !products.isEmpty(); i++) {
            User author = customers.get(i % customers.size());
            Product product = products.get(i % products.size());
            save(author, product, Integer.parseInt(PRODUCT_REVIEWS[i][0]), PRODUCT_REVIEWS[i][1],
                    STORE_FEEDBACK.length + i);
            created++;
        }
        log.info("Seeded {} demo feedback rows (store + product reviews).", created);
    }

    private List<User> activeCustomers() {
        return userRepository.findByRoleOrderByCreatedAtAsc(Role.CUSTOMER).stream()
                .filter(u -> u.getStatus() != UserStatus.INACTIVE)
                .toList();
    }

    /** {@code ageIndex} staggers createdAt (newest first) so the "recent" list looks natural. */
    private void save(User user, Product product, int rating, String message, int ageIndex) {
        Feedback f = new Feedback();
        f.setUser(user);
        f.setProduct(product); // null = store feedback
        f.setRating(rating);
        f.setMessage(message);
        f.setCreatedAt(Instant.now().minus(ageIndex, ChronoUnit.DAYS));
        feedbackRepository.save(f);
    }
}
