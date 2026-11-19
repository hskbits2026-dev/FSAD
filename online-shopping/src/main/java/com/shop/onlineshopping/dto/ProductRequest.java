package com.shop.onlineshopping.dto;

import com.shop.onlineshopping.entity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String name,
        @Size(max = 1000) String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull @Min(0) Integer qoh,
        @NotNull Category category,
        // Optional Base64 data-URL image. ~1.4M chars ≈ a 1 MB image once encoded — the cap stops a
        // huge upload from bloating the row and slowing the catalog list. null/blank = no image.
        @Size(max = 1_400_000) String image
) {}
