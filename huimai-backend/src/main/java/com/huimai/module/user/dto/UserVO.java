package com.huimai.module.user.dto;

import lombok.Data;

/**
 * 用户对外视图。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer gender;
    private Long followerCount;
    private Long followingCount;
    private Long videoCount;
    /** 当前登录用户是否已关注该用户 */
    private Boolean followed;
}
