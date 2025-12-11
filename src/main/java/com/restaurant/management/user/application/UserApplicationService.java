package com.restaurant.management.user.application;

import com.restaurant.management.user.application.command.CreateUserCommand;
import com.restaurant.management.user.application.command.UpdateUserCommand;
import com.restaurant.management.user.domain.model.User;
import com.restaurant.management.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(CreateUserCommand command) {
        userRepository.findByMobile(command.getMobile()).ifPresent(u -> {
            throw new IllegalArgumentException("手机号已存在");
        });
        User user = User.create(command.getMobile(), command.getName(), command.getNickname());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UpdateUserCommand command) {
        User user = userRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.updateProfile(command.getName(), command.getNickname());
        if (command.getEnable() != null) {
            if (Boolean.TRUE.equals(command.getEnable())) {
                user.enable();
            } else {
                user.disable();
            }
        }
        return userRepository.save(user);
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    public User getByMobile(String mobile) {
        return userRepository.findByMobile(mobile)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }
}

