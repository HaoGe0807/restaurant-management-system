package com.restaurant.management.user.domain.repository;

import com.restaurant.management.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByMobile(String mobile);

    List<User> findAll();
}

