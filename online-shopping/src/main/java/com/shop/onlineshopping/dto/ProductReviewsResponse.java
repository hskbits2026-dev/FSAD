package com.shop.onlineshopping.dto;

import com.shop.onlineshopping.entity.Feedback;

import java.time.Instant;
import java.util.List;

/**
 * Public, privacy-safe shape for a product's reviews. Product pages are viewable WITHOUT login,
 * so this deliberately exposes only the reviewer's display name (fullName) — never their email.
 */
public record ProductReviewsResponse(
        double averageRating,
        long count,
        List<ReviewItem> items
) {
    public record ReviewItem(
            String reviewerName,
            int rating,
            String message,
            Instant createdAt
    ) {
        public static ReviewItem from(Feedback f) {
            return new ReviewItem(
                    f.getUser().getFullName(),
                    f.getRating(),
                    f.getMessage(),
                    f.getCreatedAt());
        }
    }

    /** Builds the summary (average + count + items) from a product's feedback rows. */
    public static ProductReviewsResponse of(List<Feedback> feedback) {
        List<ReviewItem> items = feedback.stream().map(ReviewItem::from).toList();
        double average = feedback.stream().mapToInt(Feedback::getRating).average().orElse(0.0);
        return new ProductReviewsResponse(average, items.size(), items);
    }
}
