# 回脉 · 家庭友好的短视频平台

> 回脉，寓意回归正向价值的精神脉络。它是一个以伊斯兰价值观为精神内核、家庭友好、知识导向、内容合规（Halal）的通用短视频平台。

## 一、产品定位：伊斯兰是“内核”，不是“内容边界”

回脉**不是只面向宗教题材的短视频平台**，而是一个面向所有用户、内容多元的通用短视频社区。伊斯兰价值观的作用体现在两个层面：

- **精神内核**：倡导真诚、有益、家庭和睦、终身学习、善行公益，让平台始终保持正向、健康的氛围
- **审核依据（Halal）**：以伊斯兰伦理作为内容审核标尺，对所有内容一视同仁地把关
  - 拒绝：色情低俗、暴力血腥、赌博、谎言谣言、背谈诽谤、炫富、利息（Riba）相关推广等
  - 倡导：全年龄适宜、家庭友好、真实可信、知识与正能量
  - 配套：内容审核 + 举报 + 青少年模式，公益捐赠（Sadaqah）替代赌博式打赏

因此，平台上的生活、知识、家庭、美食、旅行、科技等各类内容都可以发布，只要“有益且合规”。同时为穆斯林用户提供礼拜时间、斋月等友好功能（特色服务，而非使用门槛）。

## 二、核心特色

- **多元内容频道**：9 个家庭友好的通用频道——知识学习、家庭亲子、美食厨房、旅行风光、历史人文、科技数码、健康生活、艺术设计、公益善行
- **内容来源溯源**：知识型视频可标注权威出处（经典、文献、官方资料等，source_type 区分 quran/hadith/other），做到“言必有据”
- **家庭友好与合规**：基于伊斯兰伦理的审核机制，全年龄适宜，青少年模式
- **穆斯林友好（规划中）**：五番礼拜时间与朝向、礼拜免打扰、斋月模式、希吉来历、阿中双语

## 三、技术栈

| 层 | 技术 |
|---|---|
| 移动 H5 | Vue 3 + Vite 6 + TypeScript + Pinia + Vue Router + Vant 4 |
| 后端 | Spring Boot 3.3.5 + Spring Security + JWT + MyBatis-Plus 3.5.9 |
| 数据库 | MySQL 8.0 + Redis 7 |
| 视频 | 本地存储 + 静态资源映射；FFmpeg 可选（自动探测，未安装则原样存储） |
| 接口文档 | Knife4j / OpenAPI 3 |
| 部署 | 预留 Docker Compose |

## 四、目录结构

```
D:\回脉
├── huimai-backend          # Spring Boot 后端
│   ├── pom.xml
│   └── src\main
│       ├── java\com\huimai
│       │   ├── common      # 统一返回、异常、基础实体
│       │   ├── config      # 安全、Redis、MyBatis、初始化器
│       │   ├── security    # JWT 工具、过滤器、上下文
│       │   └── module
│       │       ├── user          # 用户与认证
│       │       ├── video         # 视频、分类、标签、内容来源
│       │       ├── interaction   # 点赞、收藏、评论、关注
│       │       └── file          # 文件存储
│       └── resources\application.yml
├── huimai-frontend         # 移动 H5 前端
│   ├── package.json
│   └── src
│       ├── api / router / stores / styles / types / utils
│       ├── components      # 视频卡片、评论面板
│       ├── layouts         # 底部标签布局
│       └── views           # 登录、信息流、发现、发布、我的、用户主页
├── docs
│   ├── schema.sql          # 数据库结构
│   └── 架构设计.md
├── storage                 # 运行后生成：video / cover / avatar
├── start-all.ps1           # 一键启动
└── stop-all.ps1            # 一键停止
```

## 五、快速开始

### 方式一：一键启动（推荐）

```powershell
.\start-all.ps1
```

脚本会自动启动 Redis、后端（java -jar）、前端，并在独立窗口中运行。

### 方式二：手动分步启动

```powershell
# 1. 启动 Redis（路径见 start-all.ps1）
# 2. 后端（先打包再运行，中文路径下勿用 spring-boot:run）
$env:JAVA_HOME = "D:\career(1)\jdk-17.0.20.1+1"
& "D:\career\apache-maven-3.9.16\bin\mvn.cmd" -f .\huimai-backend\pom.xml package -DskipTests
& "$env:JAVA_HOME\bin\java.exe" -jar .\huimai-backend\target\huimai-backend-0.1.0.jar
# 3. 前端（新开窗口）
cd .\huimai-frontend
npm install
npm run dev
```

### 访问地址

| 用途 | 地址 |
|---|---|
| 前端页面 | http://localhost:5174/ |
| 接口文档 | http://localhost:9090/api/doc.html |
| 基础路径 | http://localhost:9090/api |

### 演示账号

- 普通用户：`test` / `123456`
- 管理员：`admin` / `admin123`

> 首次启动会自动初始化 10 个频道与上述账号。数据库脚本见 `docs/schema.sql`。

## 六、主要接口

| 模块 | 方法与路径 |
|---|---|
| 认证 | `POST /auth/register`、`POST /auth/login` |
| 视频 | `GET /videos/feed`、`GET /videos/{id}`、`POST /videos/publish`、`POST /videos/{id}/play` |
| 分类 | `GET /categories`、`GET /videos/category/{id}` |
| 互动 | `POST /interaction/like/{id}`、`/favorite/{id}`、`/follow/{id}` |
| 评论 | `POST /comments/{videoId}`、`GET /comments/list/{videoId}` |
| 用户 | `GET /user/info`、`GET /user/profile/{id}`、`PUT /user/update` |

## 七、后续路线图

- **M3 穆斯林友好能力**：礼拜时间与朝向、斋月模式、希吉来历、阿中双语切换
- **M4 审核后台**：独立 Element Plus 管理端，视频审核、举报处理、数据看板
- **M5 工程化**：推荐 Feed 流（Redis）、阿里云 OSS/VOD、FFmpeg 转码多清晰度、Docker 部署
- **远期**：AI 内容审核、内容来源知识图谱、公益捐赠（Sadaqah）合规通道
