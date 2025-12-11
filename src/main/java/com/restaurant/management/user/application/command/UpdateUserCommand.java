package com.restaurant.management.user.application.command;

import lombok.Data;

@Data
public class UpdateUserCommand {
    private Long id;
    private String name;
    private String nickname;
    private Boolean enable; // true 启用，false 禁用
}

