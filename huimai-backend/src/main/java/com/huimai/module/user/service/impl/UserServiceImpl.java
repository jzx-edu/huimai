package com.huimai.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huimai.common.BusinessException;
import com.huimai.module.user.dto.LoginDTO;
import com.huimai.module.user.dto.LoginVO;
import com.huimai.module.user.dto.RegisterDTO;
import com.huimai.module.user.dto.UserVO;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.mapper.UserMapper;
import com.huimai.module.user.service.UserService;
import com.huimai.module.interaction.entity.UserFollow;
import com.huimai.module.interaction.mapper.UserFollowMapper;
import com.huimai.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserFollowMapper userFollowMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        Long exists = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException("用户名已被占用");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null && !dto.getNickname().isBlank()
                ? dto.getNickname() : dto.getUsername());
        user.setRole("user");
        user.setStatus(1);
        user.setGender(0);
        user.setFollowerCount(0L);
        user.setFollowingCount(0L);
        user.setVideoCount(0L);
        this.save(user);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        return buildLoginVO(user);
    }

    @Override
    public UserVO getCurrentUserInfo(Long userId) {
        User user = mustGet(userId);
        return toVO(user, null);
    }

    @Override
    public UserVO getUserProfile(Long targetUserId, Long currentUserId) {
        User user = mustGet(targetUserId);
        boolean followed = false;
        // 仅在登录用户查看他人主页时，查询是否已存在关注关系
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            Long cnt = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                    .eq(UserFollow::getFollowerId, currentUserId)
                    .eq(UserFollow::getFollowingId, targetUserId));
            followed = cnt != null && cnt > 0;
        }
        return toVO(user, followed);
    }

    @Override
    public UserVO toVO(User user, Boolean followed) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setGender(user.getGender());
        vo.setFollowerCount(user.getFollowerCount());
        vo.setFollowingCount(user.getFollowingCount());
        vo.setVideoCount(user.getVideoCount());
        vo.setFollowed(followed);
        return vo;
    }

    @Override
    public List<UserVO> followingList(Long targetUserId, Long currentUserId) {
        mustGet(targetUserId);
        List<UserFollow> rels = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, targetUserId)
                .orderByDesc(UserFollow::getCreatedAt)
                .orderByDesc(UserFollow::getId));
        List<Long> ids = rels.stream().map(UserFollow::getFollowingId).collect(Collectors.toList());
        return buildUserVOList(ids, currentUserId);
    }

    @Override
    public List<UserVO> followersList(Long targetUserId, Long currentUserId) {
        mustGet(targetUserId);
        List<UserFollow> rels = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowingId, targetUserId)
                .orderByDesc(UserFollow::getCreatedAt)
                .orderByDesc(UserFollow::getId));
        List<Long> ids = rels.stream().map(UserFollow::getFollowerId).collect(Collectors.toList());
        return buildUserVOList(ids, currentUserId);
    }

    private List<UserVO> buildUserVOList(List<Long> ids, Long currentUserId) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> users = this.listByIds(ids);
        Map<Long, User> map = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 当前登录用户已关注的全部用户，用于标记列表项的关注状态
        Set<Long> myFollowing = loadMyFollowing(currentUserId);

        List<UserVO> result = new ArrayList<>();
        for (Long id : ids) {
            User u = map.get(id);
            if (u != null) {
                result.add(toVO(u, myFollowing.contains(id)));
            }
        }
        return result;
    }

    @Override
    public List<UserVO> searchUsers(String keyword, Long currentUserId) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> users = this.list(new LambdaQueryWrapper<User>()
                .nested(w -> w.like(User::getNickname, kw).or().like(User::getUsername, kw))
                .last("limit 30"));
        if (users.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> myFollowing = loadMyFollowing(currentUserId);
        List<UserVO> result = new ArrayList<>();
        for (User u : users) {
            result.add(toVO(u, myFollowing.contains(u.getId())));
        }
        return result;
    }

    private Set<Long> loadMyFollowing(Long currentUserId) {
        Set<Long> myFollowing = new HashSet<>();
        if (currentUserId != null) {
            userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                    .eq(UserFollow::getFollowerId, currentUserId))
                    .forEach(r -> myFollowing.add(r.getFollowingId()));
        }
        return myFollowing;
    }

    private User mustGet(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private LoginVO buildLoginVO(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}
