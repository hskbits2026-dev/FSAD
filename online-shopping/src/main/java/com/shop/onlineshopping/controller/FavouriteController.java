package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.dto.FavouriteResponse;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.UserRepository;
import com.shop.onlineshopping.service.FavouriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The current customer's wishlist. Products are keyed by productId (not a favourite-row id). */
@RestController
@RequestMapping("/api/favourites")
public class FavouriteController {

    private final FavouriteService favouriteService;
    private final UserRepository userRepository;

    public FavouriteController(FavouriteService favouriteService, UserRepository userRepository) {
        this.favouriteService = favouriteService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<FavouriteResponse> list(Authentication authentication) {
        return favouriteService.list(currentUser(authentication)).stream()
                .map(FavouriteResponse::from).toList();
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> add(Authentication authentication, @PathVariable Long productId) {
        favouriteService.add(currentUser(authentication), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(Authentication authentication, @PathVariable Long productId) {
        favouriteService.remove(currentUser(authentication), productId);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
