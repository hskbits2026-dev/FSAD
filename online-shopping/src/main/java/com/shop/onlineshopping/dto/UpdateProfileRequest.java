package com.shop.onlineshopping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Edit-Profile payload. Email is NOT editable (it's the login id), so it's not here. */
public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "phone must be 10 digits") String phone
) {}
