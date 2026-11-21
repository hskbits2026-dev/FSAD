package com.shop.onlineshopping.service;

import com.shop.onlineshopping.entity.Favourite;
import com.shop.onlineshopping.entity.Product;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.FavouriteRepository;
import com.shop.onlineshopping.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceTest {

    @Mock FavouriteRepository favouriteRepository;
    @Mock ProductRepository productRepository;
    @InjectMocks FavouriteService favouriteService;

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private Product product(Long id) {
        Product p = new Product();
        p.setId(id);
        return p;
    }

    @Test
    void add_newFavourite_saves() {
        User user = user(1L);
        Product product = product(2L);
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(favouriteRepository.existsByUserAndProduct(user, product)).thenReturn(false);

        favouriteService.add(user, 2L);

        verify(favouriteRepository).save(any(Favourite.class));
    }

    @Test
    void add_duplicate_isNoOp() {
        User user = user(1L);
        Product product = product(2L);
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(favouriteRepository.existsByUserAndProduct(user, product)).thenReturn(true);

        favouriteService.add(user, 2L);

        verify(favouriteRepository, never()).save(any(Favourite.class));
    }

    @Test
    void add_unknownProduct_throwsResourceNotFound() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteService.add(user(1L), 404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void remove_existingProduct_deletes() {
        User user = user(1L);
        Product product = product(2L);
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        favouriteService.remove(user, 2L);

        verify(favouriteRepository).deleteByUserAndProduct(user, product);
    }

    @Test
    void remove_unknownProduct_isNoOp() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        favouriteService.remove(user(1L), 404L);

        verify(favouriteRepository, never()).deleteByUserAndProduct(any(), any());
    }
}
