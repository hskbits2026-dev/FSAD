package com.shop.onlineshopping.service;

import com.shop.onlineshopping.dto.AddressRequest;
import com.shop.onlineshopping.entity.Address;
import com.shop.onlineshopping.entity.User;
import com.shop.onlineshopping.exception.ResourceNotFoundException;
import com.shop.onlineshopping.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock AddressRepository addressRepository;
    @InjectMocks AddressService addressService;

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private Address address(Long id, User owner, boolean isDefault) {
        Address a = new Address();
        a.setId(id);
        a.setUser(owner);
        a.setDefault(isDefault);
        return a;
    }

    private AddressRequest req() {
        return new AddressRequest("Home", "Aarav", "9876543210", "12 MG Rd", null,
                "Bengaluru", "Karnataka", "560001");
    }

    @Test
    void create_firstAddress_becomesDefault() {
        User user = user(1L);
        when(addressRepository.findByUserOrderByIsDefaultDescIdAsc(user)).thenReturn(List.of());
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address created = addressService.create(user, req());

        assertThat(created.isDefault()).isTrue();
    }

    @Test
    void create_secondAddress_isNotDefault() {
        User user = user(1L);
        when(addressRepository.findByUserOrderByIsDefaultDescIdAsc(user))
                .thenReturn(List.of(address(5L, user, true)));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address created = addressService.create(user, req());

        assertThat(created.isDefault()).isFalse();
    }

    @Test
    void setDefault_clearsFlagOnOthers() {
        User user = user(1L);
        Address oldDefault = address(5L, user, true);
        Address target = address(6L, user, false);
        when(addressRepository.findByIdAndUser(6L, user)).thenReturn(Optional.of(target));
        when(addressRepository.findByUserOrderByIsDefaultDescIdAsc(user))
                .thenReturn(List.of(oldDefault, target));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address result = addressService.setDefault(user, 6L);

        assertThat(result.isDefault()).isTrue();
        assertThat(oldDefault.isDefault()).isFalse();
    }

    @Test
    void delete_default_promotesNextAddress() {
        User user = user(1L);
        Address toDelete = address(5L, user, true);
        Address survivor = address(6L, user, false);
        when(addressRepository.findByIdAndUser(5L, user)).thenReturn(Optional.of(toDelete));
        when(addressRepository.findByUserOrderByIsDefaultDescIdAsc(user)).thenReturn(List.of(survivor));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        addressService.delete(user, 5L);

        assertThat(survivor.isDefault()).isTrue();
        verify(addressRepository).delete(toDelete);
    }

    @Test
    void delete_nonDefault_doesNotPromote() {
        User user = user(1L);
        Address toDelete = address(6L, user, false);
        when(addressRepository.findByIdAndUser(6L, user)).thenReturn(Optional.of(toDelete));

        addressService.delete(user, 6L);

        verify(addressRepository).delete(toDelete);
        verify(addressRepository, never()).findByUserOrderByIsDefaultDescIdAsc(any());
    }

    @Test
    void update_foreignAddress_throwsResourceNotFound() {
        User attacker = user(2L);
        when(addressRepository.findByIdAndUser(5L, attacker)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.update(attacker, 5L, req()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
