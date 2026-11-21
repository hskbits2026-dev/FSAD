package com.shop.onlineshopping.config;

import com.shop.onlineshopping.entity.Address;
import com.shop.onlineshopping.entity.Role;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.entity.UserStatus;
import com.shop.onlineshopping.repository.AddressRepository;
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
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerSeeder(UserRepository userRepository, AddressRepository addressRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        backfillNullStatus();
        retireJunkAccounts();
        seedRealisticCustomer("Aarav Sharma", "aarav.sharma@gmail.com", "Aarav@123");
        seedContactAndAddresses();
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

    /**
     * Give every ACTIVE customer a demo phone (if missing) and two sample addresses (if they have
     * none), so the new profile/checkout/admin features have data to show. Idempotent: a customer
     * who already has a phone keeps it, and one who already has any address gets no new ones.
     */
    private void seedContactAndAddresses() {
        // A small pool of realistic Indian addresses; each customer gets a deterministic slice.
        String[][] pool = {
                {"Home", "12, 2nd Cross, Indiranagar", "Bengaluru", "Karnataka", "560038"},
                {"Work", "4th Floor, Prestige Tower, MG Road", "Bengaluru", "Karnataka", "560001"},
                {"Home", "7, Carter Road, Bandra West", "Mumbai", "Maharashtra", "400050"},
                {"Office", "Cyber Hub, DLF Phase 2", "Gurugram", "Haryana", "122002"},
                {"Home", "22, Park Street", "Kolkata", "West Bengal", "700016"},
                {"Home", "9, Anna Nagar East", "Chennai", "Tamil Nadu", "600102"},
        };

        List<User> customers = userRepository.findByRoleOrderByCreatedAtAsc(Role.CUSTOMER);
        long phoneSeed = 9800000000L;
        int idx = 0;
        for (User u : customers) {
            if (u.getStatus() == UserStatus.INACTIVE) {
                continue; // leave retired accounts alone
            }
            boolean changed = false;
            if (u.getPhone() == null || u.getPhone().isBlank()) {
                u.setPhone(String.valueOf(phoneSeed + u.getId())); // deterministic 10-digit number
                changed = true;
            }
            if (changed) {
                userRepository.save(u);
            }

            if (addressRepository.findByUserOrderByIsDefaultDescIdAsc(u).isEmpty()) {
                String[] a1 = pool[idx % pool.length];
                String[] a2 = pool[(idx + 1) % pool.length];
                addressRepository.save(buildAddress(u, a1, u.getFullName(), u.getPhone(), true));
                addressRepository.save(buildAddress(u, a2, u.getFullName(), u.getPhone(), false));
                log.info("Seeded 2 demo addresses for customer {} <{}>.", u.getFullName(), u.getEmail());
            }
            idx++;
        }
    }

    private Address buildAddress(User user, String[] data, String recipient, String phone, boolean isDefault) {
        Address a = new Address();
        a.setUser(user);
        a.setLabel(data[0]);
        a.setRecipientName(recipient);
        a.setPhone(phone);
        a.setLine1(data[1]);
        a.setCity(data[2]);
        a.setState(data[3]);
        a.setPincode(data[4]);
        a.setDefault(isDefault);
        return a;
    }
}
