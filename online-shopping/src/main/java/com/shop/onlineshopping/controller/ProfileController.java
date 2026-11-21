package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.dto.AddressResponse;
import com.shop.onlineshopping.dto.ProfileResponse;
import com.shop.onlineshopping.dto.UpdateProfileRequest;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.UserRepository;
import com.shop.onlineshopping.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The logged-in customer's own profile. A {@code @RestController} is the "front door" — it maps
 * HTTP requests to Java methods and returns JSON. Which user? We never trust the request body for
 * that; we read it from the verified JWT via {@code authentication.getName()} (the email).
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final AddressService addressService;

    public ProfileController(UserRepository userRepository, AddressService addressService) {
        this.userRepository = userRepository;
        this.addressService = addressService;
    }

    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {
        User user = currentUser(authentication);
        return ProfileResponse.from(user, addresses(user));
    }

    @PutMapping
    public ProfileResponse updateProfile(Authentication authentication,
                                         @Valid @RequestBody UpdateProfileRequest req) {
        User user = currentUser(authentication);
        user.setFullName(req.fullName());
        user.setPhone(req.phone());
        user = userRepository.save(user);
        return ProfileResponse.from(user, addresses(user));
    }

    private List<AddressResponse> addresses(User user) {
        return addressService.listForUser(user).stream().map(AddressResponse::from).toList();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
