package com.shop.onlineshopping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.onlineshopping.dto.FeedbackRequest;
import com.shop.onlineshopping.entity.Category;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.Role;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.repository.FeedbackRepository;
import com.shop.onlineshopping.repository.ProductRepository;
import com.shop.onlineshopping.repository.UserRepository;
import com.shop.onlineshopping.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductFeedbackControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String customerToken;
    private Long productId;

    @BeforeEach
    void setUp() {
        feedbackRepository.deleteAll();
        userRepository.deleteAll();
        User customer = newUser("reviewer@shop.com");
        customerToken = jwtService.generateToken(customer);
        productId = newProduct().getId();
    }

    @AfterEach
    void tearDown() {
        feedbackRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User newUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setFullName("Riya Sharma");
        u.setRole(Role.CUSTOMER);
        return userRepository.save(u);
    }

    private Product newProduct() {
        Product p = new Product();
        p.setName("Test Widget");
        p.setDescription("desc");
        p.setPrice(new BigDecimal("99.00"));
        p.setQoh(10);
        p.setCategory(Category.ELECTRONICS);
        return productRepository.save(p);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    void getReviews_isPublic_returns200EvenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products/{id}/feedback", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.averageRating").value(0.0));
    }

    @Test
    void postReview_anonymous_returns401() throws Exception {
        mockMvc.perform(post("/api/products/{id}/feedback", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new FeedbackRequest("Nice", 5))))
                .andExpect(status().isUnauthorized());
        assertThat(feedbackRepository.count()).isZero();
    }

    @Test
    void postReview_authenticated_created_thenVisibleInPublicListWithoutEmail() throws Exception {
        mockMvc.perform(post("/api/products/{id}/feedback", productId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new FeedbackRequest("Really good widget", 4))))
                .andExpect(status().isCreated());

        assertThat(feedbackRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/products/{id}/feedback", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.items[0].reviewerName").value("Riya Sharma"))
                .andExpect(jsonPath("$.items[0].rating").value(4))
                .andExpect(jsonPath("$.items[0].message").value("Really good widget"))
                // Public reviews must NOT leak the reviewer's email.
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("reviewer@shop.com"))));
    }

    @Test
    void postReview_unknownProduct_returns404() throws Exception {
        mockMvc.perform(post("/api/products/{id}/feedback", 999999)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new FeedbackRequest("Nope", 3))))
                .andExpect(status().isNotFound());
        assertThat(feedbackRepository.count()).isZero();
    }
}
