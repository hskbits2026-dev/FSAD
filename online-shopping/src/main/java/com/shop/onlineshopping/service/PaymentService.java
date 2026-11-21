package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.CheckoutResponse;
import com.shop.onlineshopping.entity.Address;
import com.shop.onlineshopping.entity.Cart;
import com.shop.onlineshopping.entity.CartItem;
import com.shop.onlineshopping.entity.Order;
import com.shop.onlineshopping.entity.OrderItem;
import com.shop.onlineshopping.entity.OrderStatus;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.EmptyCartException;
import com.shop.onlineshopping.exception.InsufficientStockException;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.AddressRepository;
import com.shop.onlineshopping.repository.CartRepository;
import com.shop.onlineshopping.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final StripeGateway stripeGateway;

    public PaymentService(CartRepository cartRepository,
                          OrderRepository orderRepository,
                          AddressRepository addressRepository,
                          StripeGateway stripeGateway) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.stripeGateway = stripeGateway;
    }

    @Transactional
    public CheckoutResponse createCheckout(User user, Long addressId) {
        Cart cart = cartRepository.findByUser(user)
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(() -> new EmptyCartException("Cart is empty"));

        // Validate stock BEFORE creating any order or Stripe session.
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getQoh()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName());
            }
        }

        // Resolve the delivery address (must exist and belong to this customer).
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setOrderDate(Instant.now());
        // Snapshot the address onto the order so later edits/deletes can't rewrite history.
        order.setShipRecipient(address.getRecipientName());
        order.setShipPhone(address.getPhone());
        order.setShipLine1(address.getLine1());
        order.setShipLine2(address.getLine2());
        order.setShipCity(address.getCity());
        order.setShipState(address.getState());
        order.setShipPincode(address.getPincode());

        BigDecimal total = BigDecimal.ZERO;
        List<StripeGateway.LineItemData> lineItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            order.getItems().add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            lineItems.add(new StripeGateway.LineItemData(
                    product.getName(), product.getPrice(), item.getQuantity()));
        }
        order.setTotalAmount(total);
        order = orderRepository.save(order);

        StripeGateway.StripeSession session = stripeGateway.createCheckoutSession(
                new StripeGateway.CheckoutRequest(user.getEmail(), lineItems));
        order.setStripeSessionId(session.id());
        orderRepository.save(order);

        return new CheckoutResponse(order.getId(), session.url());
    }
}
