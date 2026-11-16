package com.huimai.module.user.dto;

import lombok.Data;

/**
 * 登录成功返回。
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
}
