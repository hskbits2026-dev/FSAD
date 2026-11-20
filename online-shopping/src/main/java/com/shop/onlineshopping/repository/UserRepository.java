package com.shop.onlineshopping.repository;

import com.shop.onlineshopping.entity.Role;
import com.shop.onlineshopping.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /** All users with the given role, oldest first — used to list customers in the admin dashboard. */
    List<User> findByRoleOrderByCreatedAtAsc(Role role);
}
