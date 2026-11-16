package com.huimai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 回脉 —— 家庭友好的通用短视频平台后端启动类（伊斯兰价值观为审核依据）。
 */
@SpringBootApplication
@MapperScan("com.huimai.**.mapper")
@EnableAsync
public class HuimaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuimaiApplication.class, args);
    }
}
