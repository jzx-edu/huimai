package com.huimai.module.file.service;

import lombok.Builder;
import lombok.Data;

/**
 * 存储后的视频元信息。
 */
@Data
@Builder
public class StoredVideo {

    /** Web 可访问的视频地址（含 /api 前缀） */
    private String videoUrl;
    /** Web 可访问的封面地址 */
    private String coverUrl;
    /** 服务器上的视频绝对路径 */
    private String absolutePath;
    private Long duration;
    private Integer width;
    private Integer height;
    private Long fileSize;
}
