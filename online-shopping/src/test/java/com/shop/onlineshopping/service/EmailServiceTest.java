package com.shop.onlineshopping.service;

import com.shop.onlineshopping.entity.Order;
import com.shop.onlineshopping.entity.OrderItem;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock EmailTransport transport;

    private Order sampleOrder() {
        User user = new User();
        user.setEmail("buyer@shop.com");

        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(new BigDecimal("999.99"));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPriceAtPurchase(new BigDecimal("999.99"));

        Order order = new Order();
        order.setId(42L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("999.99"));
        order.getItems().add(item);
        return order;
    }

    @Test
    void sendOrderConfirmation_handsMessageToTransport() {
        EmailService emailService = new EmailService(transport);

        emailService.sendOrderConfirmation(sampleOrder());

        // Delivered to the customer, subject carries the order id, body lists the item.
        verify(transport).send(eq("buyer@shop.com"), contains("42"), contains("Laptop"));
    }

    @Test
    void sendOrderConfirmation_swallowsTransportFailure() {
        doThrow(new IllegalStateException("mail down"))
                .when(transport).send(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString());
        EmailService emailService = new EmailService(transport);

        assertThatCode(() -> emailService.sendOrderConfirmation(sampleOrder()))
                .doesNotThrowAnyException();
    }
}
