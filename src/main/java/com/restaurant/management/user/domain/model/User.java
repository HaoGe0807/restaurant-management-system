package com.restaurant.management.user.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.restaurant.management.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户聚合根（以手机号为唯一标识）
 */
@Getter
@Setter
@TableName("users")
public class User extends BaseEntity {

    /**
     * 唯一手机号
     */
    private String mobile;

    /**
     * 姓名/称呼
     */
    private String name;

    /**
     * 昵称（可选）
     */
    private String nickname;

    /**
     * 状态
     */
    private UserStatus status;

    public static User create(String mobile, String name, String nickname) {
        User user = new User();
        user.mobile = mobile;
        user.name = name;
        user.nickname = nickname;
        user.status = UserStatus.ACTIVE;
        return user;
    }

    public void updateProfile(String name, String nickname) {
        if (name != null) {
            this.name = name;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public void enable() {
        this.status = UserStatus.ACTIVE;
    }
}

