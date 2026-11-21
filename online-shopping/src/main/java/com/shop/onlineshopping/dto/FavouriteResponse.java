package com.shop.onlineshopping.dto;

import com.shop.onlineshopping.entity.Favourite;

import java.time.Instant;

/** One wishlist entry: the favourited product plus when it was saved. */
public record FavouriteResponse(
        Long favouriteId,
        Instant favouritedAt,
        ProductResponse product
) {
    public static FavouriteResponse from(Favourite f) {
        return new FavouriteResponse(
                f.getId(),
                f.getCreatedAt(),
                ProductResponse.from(f.getProduct()));
    }
}
