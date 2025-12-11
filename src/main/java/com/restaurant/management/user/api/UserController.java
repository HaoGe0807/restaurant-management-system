package com.restaurant.management.user.api;

import com.restaurant.management.user.api.dto.CreateUserRequest;
import com.restaurant.management.user.api.dto.UpdateUserRequest;
import com.restaurant.management.user.api.dto.UserResponse;
import com.restaurant.management.user.application.UserApplicationService;
import com.restaurant.management.user.application.command.CreateUserCommand;
import com.restaurant.management.user.application.command.UpdateUserCommand;
import com.restaurant.management.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "用户/会员：创建、更新、查询")
public class UserController {

    private final UserApplicationService userApplicationService;

    @PostMapping
    @Operation(summary = "创建用户（手机号唯一）")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand();
        command.setMobile(request.getMobile());
        command.setName(request.getName());
        command.setNickname(request.getNickname());
        User user = userApplicationService.createUser(command);
        return UserResponse.from(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户资料/启用禁用")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UpdateUserCommand command = new UpdateUserCommand();
        command.setId(id);
        command.setName(request.getName());
        command.setNickname(request.getNickname());
        command.setEnable(request.getEnable());
        User user = userApplicationService.updateUser(command);
        return UserResponse.from(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "按ID查询用户")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.from(userApplicationService.getUser(id));
    }

    @GetMapping("/by-mobile/{mobile}")
    @Operation(summary = "按手机号查询用户")
    public UserResponse getByMobile(@PathVariable String mobile) {
        return UserResponse.from(userApplicationService.getByMobile(mobile));
    }

    @GetMapping
    @Operation(summary = "查询全部用户（示例）")
    public List<UserResponse> listUsers() {
        return userApplicationService.listUsers().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}

