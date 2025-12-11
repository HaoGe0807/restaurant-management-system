package com.restaurant.management.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UpdateUserRequest {

    @NotNull
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "姓名/称呼", example = "张三丰")
    private String name;

    @Schema(description = "昵称", example = "三哥")
    private String nickname;

    @Schema(description = "是否启用，true 启用，false 禁用")
    private Boolean enable;
}

