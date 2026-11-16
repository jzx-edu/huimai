package com.huimai.module.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.huimai.common.BusinessException;
import com.huimai.module.interaction.entity.Favorite;
import com.huimai.module.interaction.entity.UserFollow;
import com.huimai.module.interaction.entity.VideoLike;
import com.huimai.module.interaction.mapper.FavoriteMapper;
import com.huimai.module.interaction.mapper.UserFollowMapper;
import com.huimai.module.interaction.mapper.VideoLikeMapper;
import com.huimai.module.interaction.service.InteractionService;
import com.huimai.module.notification.service.NotificationService;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.service.UserService;
import com.huimai.module.video.entity.Video;
import com.huimai.module.video.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 互动服务实现。
 */
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final VideoLikeMapper videoLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final UserFollowMapper userFollowMapper;
    private final VideoMapper videoMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long videoId, Long userId) {
        VideoLike existing = videoLikeMapper.selectOne(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getUserId, userId));
        if (existing != null) {
            videoLikeMapper.deleteById(existing.getId());
            changeVideoCount(videoId, "like_count", -1);
            return false;
        }
        VideoLike like = new VideoLike();
        like.setVideoId(videoId);
        like.setUserId(userId);
        like.setCreatedAt(LocalDateTime.now());
        videoLikeMapper.insert(like);
        changeVideoCount(videoId, "like_count", 1);
        Video likedVideo = videoMapper.selectById(videoId);
        if (likedVideo != null) {
            notificationService.create(userId, likedVideo.getUserId(), "like",
                    "赞了你的视频", likedVideo.getTitle(), videoId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long videoId, Long userId) {
        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getVideoId, videoId)
                .eq(Favorite::getUserId, userId));
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            changeVideoCount(videoId, "favorite_count", -1);
            return false;
        }
        Favorite fav = new Favorite();
        fav.setVideoId(videoId);
        fav.setUserId(userId);
        fav.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(fav);
        changeVideoCount(videoId, "favorite_count", 1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFollow(Long targetUserId, Long followerId) {
        if (targetUserId.equals(followerId)) {
            throw new BusinessException("不能关注自己");
        }
        User target = userService.getById(targetUserId);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }

        UserFollow existing = userFollowMapper.selectOne(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, targetUserId));
        if (existing != null) {
            userFollowMapper.deleteById(existing.getId());
            // 被关注者粉丝数 -1，关注者关注数 -1
            changeUserCount(targetUserId, "follower_count", -1);
            changeUserCount(followerId, "following_count", -1);
            return false;
        }
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(targetUserId);
        follow.setCreatedAt(LocalDateTime.now());
        userFollowMapper.insert(follow);
        changeUserCount(targetUserId, "follower_count", 1);
        changeUserCount(followerId, "following_count", 1);
        notificationService.create(followerId, targetUserId, "follow",
                "关注了你", null, followerId);
        return true;
    }

    private void changeVideoCount(Long videoId, String column, int delta) {
        videoMapper.update(null, new LambdaUpdateWrapper<Video>()
                .eq(Video::getId, videoId)
                .setSql(column + " = " + column + " + " + delta));
    }

    private void changeUserCount(Long userId, String column, int delta) {
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .setSql(column + " = " + column + " + " + delta));
    }
}
