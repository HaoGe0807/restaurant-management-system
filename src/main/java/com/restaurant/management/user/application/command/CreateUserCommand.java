package com.restaurant.management.user.application.command;

import lombok.Data;

@Data
public class CreateUserCommand {
    private String mobile;
    private String name;
    private String nickname;
}

