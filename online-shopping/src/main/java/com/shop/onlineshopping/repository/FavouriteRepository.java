package com.shop.onlineshopping.repository;

import com.shop.onlineshopping.entity.Favourite;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {

    List<Favourite> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndProduct(User user, Product product);

    Optional<Favourite> findByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
