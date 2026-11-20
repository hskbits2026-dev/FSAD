package com.shop.onlineshopping.repository;

import com.shop.onlineshopping.entity.Feedback;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // Admin feedback report lists every row (store feedback + product reviews). Pull BOTH the
    // user and the product in one query so mapping each row to a DTO doesn't fire extra SELECTs.
    @EntityGraph(attributePaths = {"user", "product"})
    List<Feedback> findAllByOrderByCreatedAtDesc();

    // Reviews for one product, newest first. Pulls the user in the same query so building
    // each review's reviewer name doesn't fire an extra SELECT per row.
    @EntityGraph(attributePaths = "user")
    List<Feedback> findByProductIdOrderByCreatedAtDesc(Long productId);
}
