package com.shop.onlineshopping.dto;

import java.util.List;
import java.util.Map;

/**
 * Admin feedback report. The aggregates ({@code totalCount}, {@code averageRating},
 * {@code ratingCounts}) span ALL feedback and drive the summary header. The two lists keep
 * the kinds separate so the dashboard can show them under clear headings:
 * <ul>
 *   <li>{@code storeFeedback} — general feedback about the shop (no product attached)</li>
 *   <li>{@code productReviews} — reviews tied to a specific product</li>
 * </ul>
 */
public record FeedbackReportResponse(
        Long totalCount,
        Double averageRating,
        Map<Integer, Long> ratingCounts,
        List<FeedbackResponse> storeFeedback,
        List<FeedbackResponse> productReviews
) {}
