package com.shop.onlineshopping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Incoming address from the Add/Edit Address form. {@code @NotBlank} fields are required;
 * {@code line2} is optional. Phone and pincode use light pattern checks so obviously wrong
 * values are rejected before they reach the DB.
 */
public record AddressRequest(
        @NotBlank @Size(max = 40) String label,
        @NotBlank @Size(max = 100) String recipientName,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "phone must be 10 digits") String phone,
        @NotBlank @Size(max = 200) String line1,
        @Size(max = 200) String line2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "pincode must be 6 digits") String pincode
) {}
