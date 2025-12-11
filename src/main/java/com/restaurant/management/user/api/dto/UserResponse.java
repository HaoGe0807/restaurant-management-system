package com.restaurant.management.user.api.dto;

import com.restaurant.management.user.domain.model.User;
import com.restaurant.management.user.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户响应")
public class UserResponse {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "状态")
    private UserStatus status;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setMobile(user.getMobile());
        response.setName(user.getName());
        response.setNickname(user.getNickname());
        response.setStatus(user.getStatus());
        return response;
    }
}

