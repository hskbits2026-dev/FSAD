package com.shop.onlineshopping.config;

import com.shop.onlineshopping.entity.Role;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.entity.UserStatus;
import com.shop.onlineshopping.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tidies the customer list on startup so the admin dashboard shows realistic data.
 * A {@code CommandLineRunner} just runs a block of code once, right after the app boots.
 *
 * <p>Three idempotent steps (safe to run on every restart — nothing is duplicated):
 * <ol>
 *   <li><b>Backfill:</b> any old customer row whose {@code status} is still NULL (because the
 *       column was added after they were created) becomes ACTIVE.</li>
 *   <li><b>Soft-delete junk:</b> the auto-generated test accounts are flipped to INACTIVE.
 *       We do NOT physically delete them — keeping the row preserves any order/feedback history
 *       and avoids foreign-key headaches. INACTIVE rows just show greyed in the admin UI.</li>
 *   <li><b>Seed a real customer:</b> if a nice demo customer doesn't exist yet, create one so
 *       the list isn't just the test account.</li>
 * </ol>
 *
 * <p>Runs AFTER {@link AdminSeeder} and {@link ProductSeeder} (see {@code @Order}).
 */
@Component
@Profile("!test") // demo-data tidying — never in the test profile (tests manage their own users)
@Order(3)
public class CustomerSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CustomerSeeder.class);

    /** Emails of the auto-generated test accounts to retire (soft-delete). */
    private static final List<String> JUNK_EMAILS = List.of(
            "user21449@demo.com", "user30962@demo.com");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        backfillNullStatus();
        retireJunkAccounts();
        seedRealisticCustomer("Aarav Sharma", "aarav.sharma@gmail.com", "Aarav@123");
    }

    /** Any customer row with no status yet (added before the column existed) → ACTIVE. */
    private void backfillNullStatus() {
        List<User> customers = userRepository.findByRoleOrderByCreatedAtAsc(Role.CUSTOMER);
        int fixed = 0;
        for (User u : customers) {
            if (u.getStatus() == null) {
                u.setStatus(UserStatus.ACTIVE);
                userRepository.save(u);
                fixed++;
            }
        }
        if (fixed > 0) {
            log.info("Backfilled status=ACTIVE on {} existing customer(s).", fixed);
        }
    }

    /** Flip the known test accounts to INACTIVE (soft delete). Idempotent. */
    private void retireJunkAccounts() {
        for (String email : JUNK_EMAILS) {
            userRepository.findByEmail(email).ifPresent(u -> {
                if (u.getStatus() != UserStatus.INACTIVE) {
                    u.setStatus(UserStatus.INACTIVE);
                    userRepository.save(u);
                    log.info("Soft-deleted test customer {} (status=INACTIVE).", email);
                }
            });
        }
    }

    /** Create a realistic demo customer if that email isn't already taken. */
    private void seedRealisticCustomer(String fullName, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(Role.CUSTOMER);
        u.setStatus(UserStatus.ACTIVE);
        userRepository.save(u);
        log.info("Seeded demo customer {} <{}>.", fullName, email);
    }
}
