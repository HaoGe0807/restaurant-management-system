package com.restaurant.management.user.domain.model;

/**
 * 用户状态
 */
public enum UserStatus {
    ACTIVE("正常"),
    DISABLED("禁用");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

