package com.shop.onlineshopping.repository;

import com.shop.onlineshopping.entity.Feedback;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @EntityGraph(attributePaths = "user")
    List<Feedback> findAllByOrderByCreatedAtDesc();

    // Reviews for one product, newest first. Pulls the user in the same query so building
    // each review's reviewer name doesn't fire an extra SELECT per row.
    @EntityGraph(attributePaths = "user")
    List<Feedback> findByProductIdOrderByCreatedAtDesc(Long productId);
}
