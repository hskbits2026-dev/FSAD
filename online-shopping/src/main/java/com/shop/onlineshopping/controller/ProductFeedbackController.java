package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.dto.FeedbackRequest;
import com.shop.onlineshopping.dto.FeedbackResponse;
import com.shop.onlineshopping.dto.ProductReviewsResponse;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.UserRepository;
import com.shop.onlineshopping.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Per-product reviews. These reuse the feedback machinery: a review is a {@code Feedback} row
 * tied to a product. Reading is PUBLIC (product pages are viewable without login); writing needs
 * a logged-in user. {@code GET /api/products/**} is permitted in SecurityConfig; the POST here
 * falls through to the "authenticated" rule.
 */
@RestController
@RequestMapping("/api/products/{productId}/feedback")
public class ProductFeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    public ProductFeedbackController(FeedbackService feedbackService, UserRepository userRepository) {
        this.feedbackService = feedbackService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ProductReviewsResponse list(@PathVariable Long productId) {
        return feedbackService.productReviews(productId);
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> submit(@PathVariable Long productId,
                                                   Authentication authentication,
                                                   @Valid @RequestBody FeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.submitForProduct(currentUser(authentication), productId, request));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
