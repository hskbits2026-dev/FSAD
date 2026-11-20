package com.shop.onlineshopping.controller;

import com.shop.onlineshopping.dto.CustomerSummaryResponse;
import com.shop.onlineshopping.service.AdminCustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only endpoint for the Customers tab. A {@code @RestController} is the "front door":
 * it maps an HTTP URL to a method and turns the returned object into JSON.
 *
 * <p>Path is under {@code /api/admin/**}, which {@code SecurityConfig} already locks to
 * ROLE_ADMIN — so there's no extra auth code to write here.
 */
@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(AdminCustomerService adminCustomerService) {
        this.adminCustomerService = adminCustomerService;
    }

    @GetMapping
    public List<CustomerSummaryResponse> listCustomers() {
        return adminCustomerService.listCustomers();
    }
}
