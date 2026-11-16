package com.huimai.module.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huimai.common.PageResult;
import com.huimai.module.video.dto.VideoVO;
import com.huimai.module.video.entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 视频服务。
 */
public interface VideoService extends IService<Video> {

    /**
     * 发布视频。
     *
     * @param file        视频文件
     * @param title       标题
     * @param description 描述
     * @param categoryId  分类
     * @param isKnowledge 是否知识型
     * @param tagNames    标签名（可空）
     * @param sources     内容来源（可空，引用格式 "type|reference"）
     */
    VideoVO publish(MultipartFile file, String title, String description, Long categoryId,
                    Integer isKnowledge, List<String> tagNames, List<String> sources, Long userId);

    /**
     * 竖屏信息流（已发布视频）。
     */
    PageResult<VideoVO> feed(long page, long size, Long currentUserId);

    /**
     * 视频详情。
     */
    VideoVO detail(Long id, Long currentUserId);

    /**
     * 按分类分页。
     */
    PageResult<VideoVO> byCategory(Long categoryId, long page, long size, Long currentUserId);

    /**
     * 某用户的作品。
     */
    PageResult<VideoVO> userVideos(Long userId, long page, long size, Long currentUserId);

    /**
     * 关键字搜索。
     */
    PageResult<VideoVO> search(String keyword, long page, long size, Long currentUserId);

    /**
     * 增加播放计数。
     */
    void incrementPlay(Long id, Long userId);

    /**
     * 删除本人视频（逻辑删除），并回退作者作品数。
     */
    void delete(Long videoId, Long userId);
}
