-- =====================================================================
-- 回脉 —— 商城真实后端：商品表 + 订单表
-- MySQL 8.0 / utf8mb4
-- =====================================================================
USE huimai;

-- ---------------------------------------------------------------------
-- 商品
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(128)  NOT NULL COMMENT '商品名称',
    category    VARCHAR(64)   NOT NULL COMMENT '商品分类',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    cover_image VARCHAR(512)           DEFAULT NULL COMMENT '封面图地址（可空，为空时用渐变+图标占位）',
    icon        VARCHAR(64)            DEFAULT NULL COMMENT '占位图标（Vant 图标名）',
    gradient    VARCHAR(256)           DEFAULT NULL COMMENT '占位渐变样式',
    description VARCHAR(1024)          DEFAULT NULL COMMENT '商品描述',
    stock       INT           NOT NULL DEFAULT 0 COMMENT '库存',
    sold_count  INT           NOT NULL DEFAULT 0 COMMENT '销量',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='商品';

-- ---------------------------------------------------------------------
-- 商品订单
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS product_order;
CREATE TABLE product_order (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no     VARCHAR(32)   NOT NULL COMMENT '订单号',
    user_id      BIGINT        NOT NULL COMMENT '下单用户',
    product_id   BIGINT        NOT NULL COMMENT '商品',
    quantity     INT           NOT NULL COMMENT '购买数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status       TINYINT       NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已取消',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_product (product_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='商品订单';

-- ---------------------------------------------------------------------
-- 初始商品数据（由前端演示数据迁入，封面沿用渐变+图标占位）
-- ---------------------------------------------------------------------
INSERT INTO product (name, category, price, icon, gradient, description, stock, sold_count, status) VALUES
('亲子共读绘本套装', '图书音像', 89.00, 'notes', 'linear-gradient(135deg, #2563eb, #1e40af)',
 '精选适合 3-8 岁儿童的优质绘本，亲子共读培养阅读习惯，全套 10 册精装大开本，环保油墨印刷。', 500, 1200, 1),
('家庭知识音频课', '知识课程', 199.00, 'video', 'linear-gradient(135deg, #0891b2, #155e75)',
 '涵盖历史、人文、科学的家庭通识音频课，共 60 讲，支持随时回听，适合全家一起学习。', 2000, 860, 1),
('天然蜂蜜礼盒', '健康食品', 128.00, 'hot-sale', 'linear-gradient(135deg, #d97706, #92400e)',
 '产地直采天然成熟蜂蜜，无添加蔗糖，礼盒装两罐共 1000g，营养健康，送礼自用两相宜。', 300, 2300, 1),
('儿童益智积木', '家居生活', 156.00, 'gift', 'linear-gradient(135deg, #db2777, #9d174d)',
 '大颗粒安全积木，锻炼孩子动手与空间想象能力，圆角打磨无毛刺，附赠创意拼搭手册。', 150, 640, 1),
('经典人文书籍', '图书音像', 45.00, 'newspaper', 'linear-gradient(135deg, #7c3aed, #4c1d95)',
 '中外经典人文读本，注解详尽，适合青少年及成人阅读，提升人文素养与思辨能力。', 800, 3100, 1),
('文创精装笔记本', '文创精品', 32.00, 'edit', 'linear-gradient(135deg, #059669, #065f46)',
 '复古烫金封面，180° 平摊不掉页，100g 米黄道林纸不洇墨，记录生活与灵感。', 1200, 1500, 1),
('健康杂粮礼盒', '健康食品', 98.00, 'bag', 'linear-gradient(135deg, #ca8a04, #854d0e)',
 '科学搭配八种优质杂粮，独立小包装，营养均衡，膳食养生，礼盒装共 2400g。', 400, 980, 1),
('家庭收纳套装', '家居生活', 76.00, 'shop', 'linear-gradient(135deg, #475569, #1e293b)',
 '多规格可叠加收纳盒，加厚 PP 材质，密封防潮，让衣柜、桌面整齐有序，一套 6 件。', 600, 720, 1);
