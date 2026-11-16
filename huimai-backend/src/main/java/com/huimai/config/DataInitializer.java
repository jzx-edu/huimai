package com.huimai.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huimai.module.user.entity.User;
import com.huimai.module.user.mapper.UserMapper;
import com.huimai.module.video.entity.Category;
import com.huimai.module.video.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动初始化：首次运行时写入频道分类与演示账号。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 内容频道：家庭友好的通用分类。
     * 中文名 / 阿语名 / 英文标识 / 排序
     */
    private static final String[][] CATEGORIES = {
            {"知识学习", "المعرفة", "knowledge", "1"},
            {"家庭亲子", "الأسرة", "family", "2"},
            {"美食厨房", "الطعام", "food", "3"},
            {"旅行风光", "السفر", "travel", "4"},
            {"历史人文", "التاريخ", "history", "5"},
            {"科技数码", "التقنية", "tech", "6"},
            {"健康生活", "الصحة", "health", "7"},
            {"艺术设计", "الفن", "art", "8"},
            {"公益善行", "الخير", "charity", "9"}
    };

    @Override
    public void run(String... args) {
        initCategories();
        initDemoUser("admin", "admin123", "管理员", "admin");
        initDemoUser("test", "123456", "演示用户", "user");
    }

    private void initCategories() {
        Long count = categoryMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        int sort = 1;
        for (String[] c : CATEGORIES) {
            Category category = new Category();
            category.setName(c[0]);
            category.setNameAr(c[1]);
            category.setCode(c[2]);
            category.setSort(sort++);
            category.setStatus(1);
            categoryMapper.insert(category);
        }
        log.info("已初始化 {} 个频道分类", CATEGORIES.length);
    }

    private void initDemoUser(String username, String rawPassword, String nickname, String role) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (exists != null && exists > 0) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(1);
        user.setGender(0);
        user.setFollowerCount(0L);
        user.setFollowingCount(0L);
        user.setVideoCount(0L);
        userMapper.insert(user);
        log.info("已创建演示账号: {} / {}", username, rawPassword);
    }
}
