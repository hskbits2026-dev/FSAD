package com.shop.onlineshopping.dto;

import com.shop.onlineshopping.entity.Order;
import com.shop.onlineshopping.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Instant orderDate,
        OrderStatus status,
        BigDecimal totalAmount,
        ShippingAddress shippingAddress,
        List<OrderItemResponse> items
) {
    /** Flattened shipping snapshot for display. Null for pre-feature orders. */
    public record ShippingAddress(
            String recipientName, String phone, String line1, String line2,
            String city, String state, String pincode) {}

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        ShippingAddress shipping = order.getShipLine1() == null ? null : new ShippingAddress(
                order.getShipRecipient(),
                order.getShipPhone(),
                order.getShipLine1(),
                order.getShipLine2(),
                order.getShipCity(),
                order.getShipState(),
                order.getShipPincode());
        return new OrderResponse(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                shipping,
                items);
    }
}
