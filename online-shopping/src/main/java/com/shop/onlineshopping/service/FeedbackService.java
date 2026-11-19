package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.FeedbackRequest;
import com.shop.onlineshopping.dto.FeedbackResponse;
import com.shop.onlineshopping.dto.ProductReviewsResponse;
import com.shop.onlineshopping.entity.Feedback;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.FeedbackRepository;
import com.shop.onlineshopping.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           ProductRepository productRepository,
                           NotificationService notificationService) {
        this.feedbackRepository = feedbackRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public FeedbackResponse submit(User user, FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setMessage(request.message());
        feedback.setRating(request.rating());
        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback {} submitted by user {}", saved.getId(), user.getId());
        FeedbackResponse response = FeedbackResponse.from(saved);
        notificationService.notifyNewFeedback(response);
        return response;
    }

    /** Submit a review tied to a specific product. 404s if the product doesn't exist. */
    @Transactional
    public FeedbackResponse submitForProduct(User user, Long productId, FeedbackRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setProduct(product);
        feedback.setMessage(request.message());
        feedback.setRating(request.rating());
        Feedback saved = feedbackRepository.save(feedback);
        log.info("Review {} submitted by user {} for product {}", saved.getId(), user.getId(), productId);
        FeedbackResponse response = FeedbackResponse.from(saved);
        notificationService.notifyNewFeedback(response);
        return response;
    }

    /** Public: all reviews for a product, newest first, with average + count. No emails exposed. */
    @Transactional(readOnly = true)
    public ProductReviewsResponse productReviews(Long productId) {
        return ProductReviewsResponse.of(feedbackRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> listAll() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(FeedbackResponse::from)
                .toList();
    }
}
