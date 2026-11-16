package com.huimai.module.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 资料更新请求。
 */
@Data
public class UpdateUserDTO {

    @Size(max = 64, message = "昵称过长")
    private String nickname;

    @Size(max = 255, message = "简介过长")
    private String bio;

    private String avatar;

    private Integer gender;
}
