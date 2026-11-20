package com.shop.onlineshopping.dto;

import com.shop.onlineshopping.entity.Feedback;
import com.shop.onlineshopping.entity.Product;

import java.time.Instant;

/**
 * Admin-facing view of one feedback row. Carries both the reviewer's display name and email
 * (this is only ever returned on admin-only endpoints) plus, when the feedback is a product
 * review, the product it's about. {@code productId == null} means it's general STORE feedback.
 */
public record FeedbackResponse(
        Long id,
        String userEmail,
        String userName,
        Long productId,
        String productName,
        String message,
        int rating,
        Instant createdAt
) {
    public static FeedbackResponse from(Feedback f) {
        Product product = f.getProduct(); // null = general store feedback
        return new FeedbackResponse(
                f.getId(),
                f.getUser().getEmail(),
                f.getUser().getFullName(),
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                f.getMessage(),
                f.getRating(),
                f.getCreatedAt());
    }
}
