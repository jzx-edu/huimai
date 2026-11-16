package com.huimai.module.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huimai.common.BusinessException;
import com.huimai.common.PageResult;
import com.huimai.module.file.service.FileStorageService;
import com.huimai.module.file.service.StoredVideo;
import com.huimai.module.interaction.entity.Favorite;
import com.huimai.module.interaction.entity.PlayLog;
import com.huimai.module.interaction.entity.VideoLike;
import com.huimai.module.interaction.mapper.FavoriteMapper;
import com.huimai.module.interaction.mapper.PlayLogMapper;
import com.huimai.module.interaction.mapper.VideoLikeMapper;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.service.UserService;
import com.huimai.module.video.dto.ContentSourceVO;
import com.huimai.module.video.dto.VideoVO;
import com.huimai.module.video.entity.Category;
import com.huimai.module.video.entity.ContentSource;
import com.huimai.module.video.entity.Tag;
import com.huimai.module.video.entity.Video;
import com.huimai.module.video.entity.VideoTag;
import com.huimai.module.video.mapper.CategoryMapper;
import com.huimai.module.video.mapper.ContentSourceMapper;
import com.huimai.module.video.mapper.TagMapper;
import com.huimai.module.video.mapper.VideoMapper;
import com.huimai.module.video.mapper.VideoTagMapper;
import com.huimai.module.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 视频服务实现。
 */
@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    private final FileStorageService fileStorageService;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final VideoTagMapper videoTagMapper;
    private final ContentSourceMapper contentSourceMapper;
    private final VideoLikeMapper videoLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final PlayLogMapper playLogMapper;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoVO publish(MultipartFile file, String title, String description, Long categoryId,
                           Integer isKnowledge, List<String> tagNames, List<String> sources, Long userId) {
        if (!StringUtils.hasText(title)) {
            throw new BusinessException("标题不能为空");
        }

        StoredVideo stored = fileStorageService.storeVideo(file);

        Video video = new Video();
        video.setUserId(userId);
        video.setCategoryId(categoryId);
        video.setTitle(title.trim());
        video.setDescription(description);
        video.setVideoUrl(stored.getVideoUrl());
        video.setCoverUrl(stored.getCoverUrl());
        video.setDuration(stored.getDuration());
        video.setWidth(stored.getWidth());
        video.setHeight(stored.getHeight());
        video.setFileSize(stored.getFileSize());
        video.setIsKnowledge(isKnowledge != null && isKnowledge == 1 ? 1 : 0);
        video.setStatus(1);
        video.setPlayCount(0L);
        video.setLikeCount(0L);
        video.setCommentCount(0L);
        video.setFavoriteCount(0L);
        video.setShareCount(0L);
        this.save(video);

        saveTags(video.getId(), tagNames);
        saveSources(video.getId(), sources);

        // 作者作品数 +1
        User author = new User();
        author.setId(userId);
        author.setVideoCount(1L);
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .setSql("video_count = video_count + 1"));

        return detail(video.getId(), userId);
    }

    @Override
    public PageResult<VideoVO> feed(long page, long size, Long currentUserId) {
        Page<Video> p = new Page<>(page, size);
        Page<Video> result = this.page(p, new LambdaQueryWrapper<Video>()
                .eq(Video::getStatus, 1)
                .orderByDesc(Video::getCreatedAt));
        List<VideoVO> vos = buildVOList(result.getRecords(), currentUserId);
        return PageResult.of(vos, result.getTotal(), page, size);
    }

    @Override
    public VideoVO detail(Long id, Long currentUserId) {
        Video video = this.getById(id);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        VideoVO vo = new VideoVO();
        fillBase(vo, video);

        User author = userService.getById(video.getUserId());
        if (author != null) {
            vo.setAuthorId(author.getId());
            vo.setAuthorName(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
        if (video.getCategoryId() != null) {
            Category category = categoryMapper.selectById(video.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        vo.setLiked(existsLike(id, currentUserId));
        vo.setFavorited(existsFavorite(id, currentUserId));

        List<ContentSource> sourceList = contentSourceMapper.selectList(
                new LambdaQueryWrapper<ContentSource>().eq(ContentSource::getVideoId, id));
        vo.setSources(sourceList.stream().map(this::toSourceVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public PageResult<VideoVO> byCategory(Long categoryId, long page, long size, Long currentUserId) {
        Page<Video> p = new Page<>(page, size);
        Page<Video> result = this.page(p, new LambdaQueryWrapper<Video>()
                .eq(Video::getStatus, 1)
                .eq(Video::getCategoryId, categoryId)
                .orderByDesc(Video::getCreatedAt));
        return PageResult.of(buildVOList(result.getRecords(), currentUserId),
                result.getTotal(), page, size);
    }

    @Override
    public PageResult<VideoVO> userVideos(Long userId, long page, long size, Long currentUserId) {
        Page<Video> p = new Page<>(page, size);
        Page<Video> result = this.page(p, new LambdaQueryWrapper<Video>()
                .eq(Video::getStatus, 1)
                .eq(Video::getUserId, userId)
                .orderByDesc(Video::getCreatedAt));
        return PageResult.of(buildVOList(result.getRecords(), currentUserId),
                result.getTotal(), page, size);
    }

    @Override
    public PageResult<VideoVO> search(String keyword, long page, long size, Long currentUserId) {
        if (!StringUtils.hasText(keyword)) {
            return PageResult.of(Collections.emptyList(), 0, page, size);
        }
        Page<Video> p = new Page<>(page, size);
        Page<Video> result = this.page(p, new LambdaQueryWrapper<Video>()
                .eq(Video::getStatus, 1)
                .and(w -> w.like(Video::getTitle, keyword)
                        .or().like(Video::getDescription, keyword))
                .orderByDesc(Video::getCreatedAt));
        return PageResult.of(buildVOList(result.getRecords(), currentUserId),
                result.getTotal(), page, size);
    }

    @Override
    public void incrementPlay(Long id, Long userId) {
        this.update(new LambdaUpdateWrapper<Video>()
                .eq(Video::getId, id)
                .setSql("play_count = play_count + 1"));
        PlayLog log = new PlayLog();
        log.setVideoId(id);
        log.setUserId(userId);
        log.setCreatedAt(LocalDateTime.now());
        playLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long videoId, Long userId) {
        Video video = this.getById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        if (!video.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的视频");
        }
        // 逻辑删除（@TableLogic 实际 update deleted=1），关联点赞/评论/收藏数据保留
        this.removeById(videoId);
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .setSql("video_count = video_count - 1"));
    }

    // ---------------- 内部方法 ----------------

    private List<VideoVO> buildVOList(List<Video> videos, Long currentUserId) {
        if (videos.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> authorIds = new HashSet<>();
        Set<Long> categoryIds = new HashSet<>();
        Set<Long> videoIds = new HashSet<>();
        for (Video v : videos) {
            authorIds.add(v.getUserId());
            videoIds.add(v.getId());
            if (v.getCategoryId() != null) {
                categoryIds.add(v.getCategoryId());
            }
        }

        Map<Long, User> authorMap = userService.listByIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? new HashMap<>()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        Set<Long> likedIds = currentUserId == null ? Collections.emptySet()
                : videoLikeMapper.selectList(new LambdaQueryWrapper<VideoLike>()
                        .eq(VideoLike::getUserId, currentUserId)
                        .in(VideoLike::getVideoId, videoIds))
                .stream().map(VideoLike::getVideoId).collect(Collectors.toSet());
        Set<Long> favIds = currentUserId == null ? Collections.emptySet()
                : favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, currentUserId)
                        .in(Favorite::getVideoId, videoIds))
                .stream().map(Favorite::getVideoId).collect(Collectors.toSet());

        List<VideoVO> list = new ArrayList<>();
        for (Video v : videos) {
            VideoVO vo = new VideoVO();
            fillBase(vo, v);
            User author = authorMap.get(v.getUserId());
            if (author != null) {
                vo.setAuthorId(author.getId());
                vo.setAuthorName(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
            if (v.getCategoryId() != null) {
                Category c = categoryMap.get(v.getCategoryId());
                if (c != null) {
                    vo.setCategoryName(c.getName());
                }
            }
            vo.setLiked(likedIds.contains(v.getId()));
            vo.setFavorited(favIds.contains(v.getId()));
            list.add(vo);
        }
        return list;
    }

    private void fillBase(VideoVO vo, Video v) {
        vo.setId(v.getId());
        vo.setTitle(v.getTitle());
        vo.setDescription(v.getDescription());
        vo.setVideoUrl(v.getVideoUrl());
        vo.setCoverUrl(v.getCoverUrl());
        vo.setDuration(v.getDuration());
        vo.setWidth(v.getWidth());
        vo.setHeight(v.getHeight());
        vo.setIsKnowledge(v.getIsKnowledge());
        vo.setStatus(v.getStatus());
        vo.setPlayCount(v.getPlayCount());
        vo.setLikeCount(v.getLikeCount());
        vo.setCommentCount(v.getCommentCount());
        vo.setFavoriteCount(v.getFavoriteCount());
        vo.setShareCount(v.getShareCount());
        vo.setCategoryId(v.getCategoryId());
        vo.setCreatedAt(v.getCreatedAt());
    }

    private boolean existsLike(Long videoId, Long userId) {
        if (userId == null) {
            return false;
        }
        Long count = videoLikeMapper.selectCount(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getUserId, userId));
        return count != null && count > 0;
    }

    private boolean existsFavorite(Long videoId, Long userId) {
        if (userId == null) {
            return false;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getVideoId, videoId)
                .eq(Favorite::getUserId, userId));
        return count != null && count > 0;
    }

    private void saveTags(Long videoId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        Set<String> names = tagNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());
        for (String name : names) {
            Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
            if (tag == null) {
                tag = new Tag();
                tag.setName(name);
                tag.setUseCount(1L);
                tag.setCreatedAt(LocalDateTime.now());
                tagMapper.insert(tag);
            } else {
                tagMapper.update(null, new LambdaUpdateWrapper<Tag>()
                        .eq(Tag::getId, tag.getId())
                        .setSql("use_count = use_count + 1"));
            }
            VideoTag vt = new VideoTag();
            vt.setVideoId(videoId);
            vt.setTagId(tag.getId());
            videoTagMapper.insert(vt);
        }
    }

    private void saveSources(Long videoId, List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return;
        }
        for (String raw : sources) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String[] parts = raw.split("\\|", 2);
            ContentSource cs = new ContentSource();
            cs.setVideoId(videoId);
            cs.setSourceType(parts.length == 2 ? parts[0].trim() : "other");
            cs.setReference(parts.length == 2 ? parts[1].trim() : parts[0].trim());
            cs.setCreatedAt(LocalDateTime.now());
            contentSourceMapper.insert(cs);
        }
    }

    private ContentSourceVO toSourceVO(ContentSource cs) {
        ContentSourceVO vo = new ContentSourceVO();
        vo.setId(cs.getId());
        vo.setSourceType(cs.getSourceType());
        vo.setReference(cs.getReference());
        vo.setTitleAr(cs.getTitleAr());
        vo.setNote(cs.getNote());
        return vo;
    }
}
