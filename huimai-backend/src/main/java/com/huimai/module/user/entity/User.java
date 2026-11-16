package com.huimai.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huimai.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_user")
public class User extends BaseEntity {

    private String username;

    @JsonIgnore
    private String password;

    private String nickname;
    private String avatar;
    private String bio;
    private Integer gender;
    private String phone;
    private String email;
    private String role;
    private Integer status;
    private Long followerCount;
    private Long followingCount;
    private Long videoCount;
}
