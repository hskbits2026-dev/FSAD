package com.shop.onlineshopping.dto;

import jakarta.validation.constraints.NotNull;

/** Checkout payload: which saved address to ship this order to. Required (enforced by the UI too). */
public record CheckoutRequest(
        @NotNull(message = "a delivery address is required") Long addressId
) {}
