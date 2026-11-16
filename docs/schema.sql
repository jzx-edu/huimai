-- =====================================================================
-- 回脉 —— 家庭友好的通用短视频平台 数据库结构（伊斯兰价值观为审核依据）
-- MySQL 8.0 / utf8mb4
-- =====================================================================

CREATE DATABASE IF NOT EXISTS huimai
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE huimai;

-- ---------------------------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS app_user;
CREATE TABLE app_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(64)  NOT NULL COMMENT '登录名',
    password        VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码',
    nickname        VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
    avatar          VARCHAR(512)          DEFAULT NULL COMMENT '头像地址',
    bio             VARCHAR(255)          DEFAULT NULL COMMENT '简介',
    gender          TINYINT               DEFAULT 0 COMMENT '0未知 1男 2女',
    phone           VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(128)          DEFAULT NULL COMMENT '邮箱',
    role            VARCHAR(16)  NOT NULL DEFAULT 'user' COMMENT 'user/admin',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    follower_count  BIGINT       NOT NULL DEFAULT 0 COMMENT '粉丝数',
    following_count BIGINT       NOT NULL DEFAULT 0 COMMENT '关注数',
    video_count     BIGINT       NOT NULL DEFAULT 0 COMMENT '作品数',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB COMMENT ='用户表';

-- ---------------------------------------------------------------------
-- 2. 关注关系
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS user_follow;
CREATE TABLE user_follow (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    follower_id BIGINT   NOT NULL COMMENT '粉丝（关注者）',
    following_id BIGINT  NOT NULL COMMENT '被关注者',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_follow (follower_id, following_id),
    KEY idx_following (following_id)
) ENGINE = InnoDB COMMENT ='关注关系';

-- ---------------------------------------------------------------------
-- 3. 视频分类（家庭友好的通用频道）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(64) NOT NULL COMMENT '中文频道名',
    name_ar    VARCHAR(128)         DEFAULT NULL COMMENT '阿语名',
    code       VARCHAR(64) NOT NULL COMMENT '英文标识',
    icon       VARCHAR(512)         DEFAULT NULL COMMENT '图标',
    sort       INT         NOT NULL DEFAULT 0 COMMENT '排序',
    status     TINYINT     NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE = InnoDB COMMENT ='视频分类';

-- ---------------------------------------------------------------------
-- 4. 视频
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS video;
CREATE TABLE video (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '作者',
    category_id     BIGINT                DEFAULT NULL COMMENT '分类',
    title           VARCHAR(128) NOT NULL COMMENT '标题',
    description     VARCHAR(1024)         DEFAULT NULL COMMENT '描述',
    video_url       VARCHAR(512) NOT NULL COMMENT '播放地址',
    cover_url       VARCHAR(512)          DEFAULT NULL COMMENT '封面',
    duration        BIGINT       NOT NULL DEFAULT 0 COMMENT '时长(秒)',
    width           INT          NOT NULL DEFAULT 0,
    height          INT          NOT NULL DEFAULT 0,
    file_size       BIGINT       NOT NULL DEFAULT 0 COMMENT '字节',
    is_knowledge    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否知识型内容',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0待审核 1已发布 2拒绝 3下架',
    play_count      BIGINT       NOT NULL DEFAULT 0,
    like_count      BIGINT       NOT NULL DEFAULT 0,
    comment_count   BIGINT       NOT NULL DEFAULT 0,
    favorite_count BIGINT       NOT NULL DEFAULT 0,
    share_count     BIGINT       NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_category (category_id),
    KEY idx_status_created (status, created_at)
) ENGINE = InnoDB COMMENT ='视频';

-- ---------------------------------------------------------------------
-- 5. 标签
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS tag;
CREATE TABLE tag (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(64) NOT NULL,
    use_count  BIGINT      NOT NULL DEFAULT 0,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name)
) ENGINE = InnoDB COMMENT ='标签';

-- ---------------------------------------------------------------------
-- 6. 视频-标签
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS video_tag;
CREATE TABLE video_tag (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    video_id BIGINT NOT NULL,
    tag_id   BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_video_tag (video_id, tag_id),
    KEY idx_tag (tag_id)
) ENGINE = InnoDB COMMENT ='视频标签关联';

-- ---------------------------------------------------------------------
-- 7. 内容来源（经典 / 文献 / 官方资料等权威出处）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS content_source;
CREATE TABLE content_source (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    video_id    BIGINT       NOT NULL,
    source_type VARCHAR(16)  NOT NULL DEFAULT 'other' COMMENT 'quran/hadith/other',
    reference   VARCHAR(255) NOT NULL COMMENT '出处，如 古兰经 2:255；布哈里圣训 1',
    title_ar    VARCHAR(255)          DEFAULT NULL COMMENT '阿语标题',
    note        VARCHAR(512)          DEFAULT NULL COMMENT '说明',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_video (video_id)
) ENGINE = InnoDB COMMENT ='内容来源';

-- ---------------------------------------------------------------------
-- 8. 点赞
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS video_like;
CREATE TABLE video_like (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    video_id   BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_video_user (video_id, user_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB COMMENT ='视频点赞';

-- ---------------------------------------------------------------------
-- 9. 收藏
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS favorite;
CREATE TABLE favorite (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    video_id   BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fav (video_id, user_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB COMMENT ='收藏';

-- ---------------------------------------------------------------------
-- 10. 评论
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    video_id     BIGINT        NOT NULL,
    user_id      BIGINT        NOT NULL,
    parent_id    BIGINT        NOT NULL DEFAULT 0 COMMENT '父评论，0为一级',
    content      VARCHAR(1000) NOT NULL,
    like_count   BIGINT        NOT NULL DEFAULT 0,
    reply_count  BIGINT        NOT NULL DEFAULT 0,
    status       TINYINT       NOT NULL DEFAULT 1 COMMENT '1正常 0隐藏',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_video (video_id, parent_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB COMMENT ='评论';

-- ---------------------------------------------------------------------
-- 11. 播放日志
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS play_log;
CREATE TABLE play_log (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    video_id   BIGINT   NOT NULL,
    user_id    BIGINT            DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_video (video_id)
) ENGINE = InnoDB COMMENT ='播放日志';

-- ---------------------------------------------------------------------
-- 12. 举报
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS report;
CREATE TABLE report (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    video_id    BIGINT                 DEFAULT NULL,
    comment_id  BIGINT                 DEFAULT NULL,
    user_id     BIGINT        NOT NULL COMMENT '举报人',
    reason      VARCHAR(128)  NOT NULL,
    description VARCHAR(512)           DEFAULT NULL,
    status      TINYINT       NOT NULL DEFAULT 0 COMMENT '0待处理 1已处理',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE = InnoDB COMMENT ='举报';

-- ---------------------------------------------------------------------
-- 13. 通知
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL COMMENT '接收者',
    from_user_id BIGINT                DEFAULT NULL COMMENT '触发者',
    type         VARCHAR(16)  NOT NULL COMMENT 'like/comment/follow/message/system',
    title        VARCHAR(128) NOT NULL,
    content      VARCHAR(255)          DEFAULT NULL,
    related_id   BIGINT                DEFAULT NULL,
    is_read      TINYINT      NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read),
    KEY idx_from_user (from_user_id)
) ENGINE = InnoDB COMMENT ='通知';

-- ---------------------------------------------------------------------
-- 14. 私信
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS private_message;
CREATE TABLE private_message (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    sender_id   BIGINT        NOT NULL COMMENT '发送者',
    receiver_id BIGINT        NOT NULL COMMENT '接收者',
    content     VARCHAR(1000) NOT NULL COMMENT '消息内容',
    is_read     TINYINT       NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (id),
    KEY idx_sender (sender_id),
    KEY idx_receiver_read (receiver_id, is_read),
    KEY idx_pair (sender_id, receiver_id)
) ENGINE = InnoDB COMMENT ='私信';
