package com.shop.onlineshopping.dto;

import com.shop.onlineshopping.entity.User;

import java.util.List;

/** The logged-in customer's own profile: identity fields plus their saved addresses. */
public record ProfileResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        List<AddressResponse> addresses
) {
    public static ProfileResponse from(User user, List<AddressResponse> addresses) {
        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                addresses);
    }
}
