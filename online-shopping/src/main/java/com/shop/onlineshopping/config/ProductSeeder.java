package com.shop.onlineshopping.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.onlineshopping.entity.Category;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Base64;

/**
 * Seeds a clean, demo-ready product catalog on startup — but ONLY when the products
 * table is empty. Mirrors {@link AdminSeeder}: a {@code CommandLineRunner} runs once,
 * right after the app boots.
 *
 * <p>Re-seedable by design: wipe the {@code products} table and restart the app and this
 * class repopulates it. If the table already has rows, it does nothing (so it never
 * clobbers real data or duplicates on every restart).
 *
 * <p>The catalog itself lives in {@code src/main/resources/seed-products.json} (a plain
 * list of products) and the photos live as {@code .jpg} files under
 * {@code src/main/resources/seed-images/}. Keeping the data in JSON — not as dozens of
 * Java literals — keeps this class small no matter how big the catalog grows. At seed
 * time we read each product's photo file, Base64-encode it into a "data URL" string
 * ("data:image/jpeg;base64,...") — exactly the format the admin UI produces when an
 * admin uploads a picture — and store that in the product's {@code image} column.
 */
@Component
@Order(2) // run after AdminSeeder, before Customer/OrderSeeder
public class ProductSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductSeeder.class);

    private final ProductRepository productRepository;

    public ProductSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * One catalog entry as it appears in seed-products.json. {@code image} is the
     * filename under resources/seed-images/; {@code category} is a {@link Category} name.
     */
    private record SeedProduct(String name, String description, String price, int qoh,
                               String category, String image) {
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            // Catalog already populated — leave it alone (don't duplicate on every restart).
            return;
        }
        SeedProduct[] catalog = loadCatalog();
        if (catalog.length == 0) {
            log.warn("seed-products.json was empty or unreadable; skipping product seeding.");
            return;
        }
        log.info("Products table empty — seeding {} demo products.", catalog.length);
        for (SeedProduct seed : catalog) {
            Product product = new Product();
            product.setName(seed.name());
            product.setDescription(seed.description());
            product.setPrice(new BigDecimal(seed.price()));
            product.setQoh(seed.qoh());
            product.setCategory(Category.valueOf(seed.category()));
            product.setImage(loadImageAsDataUrl(seed.image()));
            productRepository.save(product);
        }
        log.info("Seeded {} demo products.", catalog.length);
    }

    /** Reads the product list from resources/seed-products.json. Returns empty on failure. */
    private SeedProduct[] loadCatalog() {
        try (InputStream in = new ClassPathResource("seed-products.json").getInputStream()) {
            return new ObjectMapper().readValue(in, SeedProduct[].class);
        } catch (Exception e) {
            log.error("Failed to read seed-products.json: {}", e.getMessage());
            return new SeedProduct[0];
        }
    }

    /**
     * Reads a JPEG from resources/seed-images/ and returns it as a Base64 "data URL"
     * string. On any read failure we log and return {@code null} so the product still
     * seeds (the UI falls back to its gradient placeholder tile) instead of aborting.
     */
    private String loadImageAsDataUrl(String fileName) {
        try {
            byte[] bytes = new ClassPathResource("seed-images/" + fileName).getInputStream().readAllBytes();
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.warn("Could not load seed image '{}' ({}); seeding product without an image.",
                    fileName, e.getMessage());
            return null;
        }
    }
}
