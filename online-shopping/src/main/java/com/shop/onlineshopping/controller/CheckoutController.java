package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.dto.CheckoutRequest;
import com.shop.onlineshopping.dto.CheckoutResponse;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.UserRepository;
import com.shop.onlineshopping.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public CheckoutController(PaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public CheckoutResponse checkout(Authentication authentication,
                                     @Valid @RequestBody CheckoutRequest request) {
        return paymentService.createCheckout(currentUser(authentication), request.addressId());
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
