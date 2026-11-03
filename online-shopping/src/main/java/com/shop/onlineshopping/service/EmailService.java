package com.shop.onlineshopping.service;

import com.shop.onlineshopping.entity.Order;
import com.shop.onlineshopping.entity.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Builds and sends the order-confirmation email. It no longer knows *how* the mail is
 * delivered — it hands the finished message to an {@link EmailTransport} (SMTP or the
 * Mailtrap HTTP API, chosen by config). Any send failure is caught and logged here so a
 * mail problem can never break the paid-order transaction.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final EmailTransport transport;

    public EmailService(EmailTransport transport) {
        this.transport = transport;
    }

    public void sendOrderConfirmation(Order order) {
        try {
            transport.send(
                    order.getUser().getEmail(),
                    "Order confirmation #" + order.getId(),
                    buildBody(order));
            log.info("Order confirmation email sent for order {}", order.getId());
        } catch (Exception e) {
            // Never let a mail failure break the payment webhook — log and move on.
            log.error("Failed to send order confirmation email for order {}: {}", order.getId(), e.getMessage());
        }
    }

    private String buildBody(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thank you for your order #").append(order.getId()).append("!\n\n");
        sb.append("Items:\n");
        for (OrderItem item : order.getItems()) {
            sb.append("  - ")
                    .append(item.getProduct().getName())
                    .append(" x ").append(item.getQuantity())
                    .append(" @ ").append(item.getPriceAtPurchase())
                    .append("\n");
        }
        sb.append("\nTotal: ").append(order.getTotalAmount()).append("\n");
        return sb.toString();
    }
}
