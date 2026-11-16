package com.huimai.module.user.controller;

import com.huimai.common.Result;
import com.huimai.module.user.dto.UpdateUserDTO;
import com.huimai.module.user.dto.UserVO;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.service.UserService;
import com.huimai.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户信息（需登录）。
 */
@Tag(name = "用户接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "当前登录用户信息")
    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.ok(userService.getCurrentUserInfo(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "用户主页")
    @GetMapping("/profile/{id}")
    public Result<UserVO> profile(@PathVariable Long id) {
        return Result.ok(userService.getUserProfile(id, SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "关注列表")
    @GetMapping("/following/{id}")
    public Result<List<UserVO>> following(@PathVariable Long id) {
        return Result.ok(userService.followingList(id, SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "粉丝列表")
    @GetMapping("/followers/{id}")
    public Result<List<UserVO>> followers(@PathVariable Long id) {
        return Result.ok(userService.followersList(id, SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<List<UserVO>> search(@RequestParam String keyword) {
        return Result.ok(userService.searchUsers(keyword, SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "更新资料")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody UpdateUserDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setBio(dto.getBio());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        userService.updateById(user);
        return Result.ok();
    }
}
