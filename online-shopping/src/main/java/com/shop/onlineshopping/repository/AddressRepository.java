package com.shop.onlineshopping.repository;

import com.shop.onlineshopping.entity.Address;
import com.shop.onlineshopping.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    /** A customer's addresses, default first then oldest-first. */
    List<Address> findByUserOrderByIsDefaultDescIdAsc(User user);

    /** Scoped lookup: only returns the address if it belongs to this user (ownership guard). */
    Optional<Address> findByIdAndUser(Long id, User user);

    Optional<Address> findByUserAndIsDefaultTrue(User user);

    /** All addresses for a set of users — used by the admin view to avoid N+1 queries. */
    List<Address> findByUserInOrderByIsDefaultDescIdAsc(List<User> users);
}
