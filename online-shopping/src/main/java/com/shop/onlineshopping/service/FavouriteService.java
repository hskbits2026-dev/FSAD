package com.shop.onlineshopping.service;

import com.shop.onlineshopping.entity.Favourite;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.FavouriteRepository;
import com.shop.onlineshopping.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The wishlist logic. "Favouriting" is idempotent: adding a product you already favourited does
 * nothing (the DB unique constraint also guards this), and removing one you never favourited is a
 * silent no-op. That keeps the heart-toggle on the UI simple and safe to click repeatedly.
 */
@Service
public class FavouriteService {

    private static final Logger log = LoggerFactory.getLogger(FavouriteService.class);

    private final FavouriteRepository favouriteRepository;
    private final ProductRepository productRepository;

    public FavouriteService(FavouriteRepository favouriteRepository, ProductRepository productRepository) {
        this.favouriteRepository = favouriteRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Favourite> list(User user) {
        return favouriteRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void add(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (favouriteRepository.existsByUserAndProduct(user, product)) {
            return; // already a favourite — idempotent
        }
        Favourite f = new Favourite();
        f.setUser(user);
        f.setProduct(product);
        favouriteRepository.save(f);
        log.info("Customer {} favourited product {}", user.getId(), productId);
    }

    @Transactional
    public void remove(User user, Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return; // nothing to remove
        }
        favouriteRepository.deleteByUserAndProduct(user, product);
    }
}
