package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.dto.AddressRequest;
import com.shop.onlineshopping.dto.AddressResponse;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.UserRepository;
import com.shop.onlineshopping.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CRUD for the current customer's delivery addresses. All actions are user-scoped by the service. */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserRepository userRepository;

    public AddressController(AddressService addressService, UserRepository userRepository) {
        this.addressService = addressService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<AddressResponse> list(Authentication authentication) {
        return addressService.listForUser(currentUser(authentication)).stream()
                .map(AddressResponse::from).toList();
    }

    @PostMapping
    public AddressResponse create(Authentication authentication, @Valid @RequestBody AddressRequest req) {
        return AddressResponse.from(addressService.create(currentUser(authentication), req));
    }

    @PutMapping("/{id}")
    public AddressResponse update(Authentication authentication, @PathVariable Long id,
                                  @Valid @RequestBody AddressRequest req) {
        return AddressResponse.from(addressService.update(currentUser(authentication), id, req));
    }

    @PutMapping("/{id}/default")
    public AddressResponse makeDefault(Authentication authentication, @PathVariable Long id) {
        return AddressResponse.from(addressService.setDefault(currentUser(authentication), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        addressService.delete(currentUser(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
