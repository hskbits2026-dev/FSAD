package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.CheckoutResponse;
import com.shop.onlineshopping.entity.Address;
import com.shop.onlineshopping.entity.Cart;
import com.shop.onlineshopping.entity.CartItem;
import com.shop.onlineshopping.entity.OrderStatus;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.EmptyCartException;
import com.shop.onlineshopping.exception.InsufficientStockException;
import com.shop.onlineshopping.entity.Order;
import com.shop.onlineshopping.repository.AddressRepository;
import com.shop.onlineshopping.repository.CartRepository;
import com.shop.onlineshopping.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock CartRepository cartRepository;
    @Mock OrderRepository orderRepository;
    @Mock AddressRepository addressRepository;
    @Mock StripeGateway stripeGateway;
    @InjectMocks PaymentService paymentService;

    private User user() {
        User u = new User();
        u.setId(1L);
        u.setEmail("buyer@shop.com");
        return u;
    }

    private Address address(User user) {
        Address a = new Address();
        a.setId(7L);
        a.setUser(user);
        a.setRecipientName("Aarav");
        a.setPhone("9876543210");
        a.setLine1("12 MG Rd");
        a.setCity("Bengaluru");
        a.setState("Karnataka");
        a.setPincode("560001");
        return a;
    }

    private Cart cartWith(User user, int productQoh, int quantity) {
        Product product = new Product();
        product.setId(5L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setQoh(productQoh);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setId(20L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        cart.getItems().add(item);
        return cart;
    }

    @Test
    void createCheckout_happyPath_savesPendingOrderAndReturnsUrl() {
        User user = user();
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cartWith(user, 5, 2)));
        when(addressRepository.findByIdAndUser(7L, user)).thenReturn(Optional.of(address(user)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) o.setId(100L);
            return o;
        });
        when(stripeGateway.createCheckoutSession(any()))
                .thenReturn(new StripeGateway.StripeSession("cs_test_1", "https://checkout.stripe/pay"));

        CheckoutResponse response = paymentService.createCheckout(user, 7L);

        assertThat(response.orderId()).isEqualTo(100L);
        assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe/pay");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        Order saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(saved.getStripeSessionId()).isEqualTo("cs_test_1");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("1999.98");
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getPriceAtPurchase()).isEqualByComparingTo("999.99");
        assertThat(saved.getShipLine1()).isEqualTo("12 MG Rd");
        assertThat(saved.getShipPincode()).isEqualTo("560001");
    }

    @Test
    void createCheckout_emptyCart_throwsAndCreatesNoSession() {
        User user = user();
        Cart empty = new Cart();
        empty.setUser(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(empty));

        assertThatThrownBy(() -> paymentService.createCheckout(user, 7L))
                .isInstanceOf(EmptyCartException.class);
        verifyNoInteractions(stripeGateway);
    }

    @Test
    void createCheckout_noCart_throwsEmptyCart() {
        User user = user();
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createCheckout(user, 7L))
                .isInstanceOf(EmptyCartException.class);
        verifyNoInteractions(stripeGateway);
    }

    @Test
    void createCheckout_quantityExceedsStock_throwsInsufficientStockAndCreatesNoSession() {
        User user = user();
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cartWith(user, 1, 5)));

        assertThatThrownBy(() -> paymentService.createCheckout(user, 7L))
                .isInstanceOf(InsufficientStockException.class);
        verifyNoInteractions(stripeGateway);
    }
}
