package com.restaurant.management.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.restaurant.management.user.domain.model.User;
import com.restaurant.management.user.domain.repository.UserRepository;
import com.restaurant.management.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public Optional<User> findByMobile(String mobile) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile));
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        return userMapper.selectList(null);
    }
}

