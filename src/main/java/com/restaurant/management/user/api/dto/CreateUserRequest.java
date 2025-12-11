package com.restaurant.management.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "创建用户请求")
public class CreateUserRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{6,20}$", message = "手机号格式不正确")
    @Schema(description = "手机号（唯一）", example = "13800138000")
    private String mobile;

    @NotBlank
    @Schema(description = "姓名/称呼", example = "张三")
    private String name;

    @Schema(description = "昵称", example = "小三")
    private String nickname;
}

