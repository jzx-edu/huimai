package com.huimai.module.interaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huimai.common.BusinessException;
import com.huimai.common.PageResult;
import com.huimai.module.interaction.dto.CommentVO;
import com.huimai.module.interaction.entity.Comment;
import com.huimai.module.interaction.entity.CommentLike;
import com.huimai.module.interaction.mapper.CommentLikeMapper;
import com.huimai.module.interaction.mapper.CommentMapper;
import com.huimai.module.interaction.service.CommentService;
import com.huimai.module.notification.service.NotificationService;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.service.UserService;
import com.huimai.module.video.entity.Video;
import com.huimai.module.video.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现。
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final VideoMapper videoMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO add(Long videoId, Long userId, String content, Long parentId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        long parent = parentId != null ? parentId : 0L;
        if (parent > 0 && commentMapper.selectById(parent) == null) {
            throw new BusinessException("父评论不存在");
        }

        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setParentId(parent);
        comment.setContent(content.trim());
        comment.setLikeCount(0L);
        comment.setReplyCount(0L);
        comment.setStatus(1);
        commentMapper.insert(comment);

        // 视频总评论数 +1
        videoMapper.update(null, new LambdaUpdateWrapper<Video>()
                .eq(Video::getId, videoId)
                .setSql("comment_count = comment_count + 1"));
        // 父评论回复数 +1
        if (parent > 0) {
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, parent)
                    .setSql("reply_count = reply_count + 1"));
        }

        notificationService.create(userId, video.getUserId(), "comment",
                "评论了你的视频", comment.getContent(), videoId);

        User user = userService.getById(userId);
        return toVO(comment, user);
    }

    @Override
    public PageResult<CommentVO> list(Long videoId, long page, long size, Long currentUserId) {
        Page<Comment> p = new Page<>(page, size);
        Page<Comment> topPage = commentMapper.selectPage(p, new LambdaQueryWrapper<Comment>()
                .eq(Comment::getVideoId, videoId)
                .eq(Comment::getParentId, 0)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreatedAt));
        List<Comment> top = topPage.getRecords();
        if (top.isEmpty()) {
            return PageResult.of(Collections.emptyList(), topPage.getTotal(), page, size);
        }

        List<Long> topIds = top.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> replies = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getVideoId, videoId)
                .in(Comment::getParentId, topIds)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreatedAt));

        // 批量取用户信息
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        top.forEach(c -> userIds.add(c.getUserId()));
        replies.forEach(c -> userIds.add(c.getUserId()));
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 当前用户已点赞的评论 id 集合
        List<Long> allIds = new ArrayList<>(topIds);
        replies.forEach(r -> allIds.add(r.getId()));
        java.util.Set<Long> likedIds = loadLikedIds(allIds, currentUserId);

        Map<Long, List<CommentVO>> replyMap = new HashMap<>();
        for (Comment r : replies) {
            CommentVO vo = toVO(r, userMap.get(r.getUserId()));
            vo.setLiked(likedIds.contains(r.getId()));
            replyMap.computeIfAbsent(r.getParentId(), k -> new ArrayList<>()).add(vo);
        }

        List<CommentVO> result = new ArrayList<>();
        for (Comment c : top) {
            CommentVO vo = toVO(c, userMap.get(c.getUserId()));
            vo.setLiked(likedIds.contains(c.getId()));
            vo.setReplies(replyMap.getOrDefault(c.getId(), new ArrayList<>()));
            result.add(vo);
        }
        return PageResult.of(result, topPage.getTotal(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }

        // 待删除：自身 + 其下全部有效回复
        List<Long> ids = new ArrayList<>();
        ids.add(commentId);
        List<Comment> replies = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, commentId)
                .eq(Comment::getStatus, 1));
        replies.forEach(r -> ids.add(r.getId()));

        // 逻辑删除（@TableLogic 实际执行 deleted=1）
        commentMapper.deleteBatchIds(ids);

        // 视频评论数回退（含被一并删除的回复）
        videoMapper.update(null, new LambdaUpdateWrapper<Video>()
                .eq(Video::getId, comment.getVideoId())
                .setSql("comment_count = comment_count - " + ids.size()));

        // 删除的若是回复，父评论回复数 -1
        if (comment.getParentId() > 0) {
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, comment.getParentId())
                    .setSql("reply_count = reply_count - 1"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        CommentLike existing = commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId));
        if (existing != null) {
            // 已点赞 → 取消
            commentLikeMapper.deleteById(existing.getId());
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, commentId)
                    .setSql("like_count = like_count - 1"));
            return false;
        }
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        commentLikeMapper.insert(like);
        commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                .eq(Comment::getId, commentId)
                .setSql("like_count = like_count + 1"));
        return true;
    }

    private java.util.Set<Long> loadLikedIds(List<Long> commentIds, Long currentUserId) {
        if (currentUserId == null || commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        return commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getUserId, currentUserId)
                        .in(CommentLike::getCommentId, commentIds))
                .stream().map(CommentLike::getCommentId)
                .collect(Collectors.toSet());
    }

    private CommentVO toVO(Comment c, User user) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setVideoId(c.getVideoId());
        vo.setUserId(c.getUserId());
        vo.setParentId(c.getParentId());
        vo.setContent(c.getContent());
        vo.setLikeCount(c.getLikeCount());
        vo.setReplyCount(c.getReplyCount());
        vo.setCreatedAt(c.getCreatedAt());
        if (user != null) {
            vo.setUserName(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        return vo;
    }
}
