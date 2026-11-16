package com.huimai.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huimai.module.user.dto.LoginDTO;
import com.huimai.module.user.dto.LoginVO;
import com.huimai.module.user.dto.RegisterDTO;
import com.huimai.module.user.dto.UserVO;
import com.huimai.module.user.entity.User;

import java.util.List;

/**
 * 用户服务。
 */
public interface UserService extends IService<User> {

    LoginVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO getCurrentUserInfo(Long userId);

    UserVO getUserProfile(Long targetUserId, Long currentUserId);

    UserVO toVO(User user, Boolean followed);

    /** 某用户的关注列表 */
    List<UserVO> followingList(Long targetUserId, Long currentUserId);

    /** 某用户的粉丝列表 */
    List<UserVO> followersList(Long targetUserId, Long currentUserId);

    /** 按用户名/昵称模糊搜索用户 */
    List<UserVO> searchUsers(String keyword, Long currentUserId);
}
