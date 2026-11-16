package com.huimai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 回脉业务配置属性。
 */
@Data
@Component
@ConfigurationProperties(prefix = "huimai")
public class HuimaiProperties {

    private Storage storage = new Storage();
    private Ffmpeg ffmpeg = new Ffmpeg();

    @Data
    public static class Storage {
        private String root;
        private String videoDir;
        private String coverDir;
        private String avatarDir;
    }

    @Data
    public static class Ffmpeg {
        /** ffmpeg 可执行文件路径，空则从 PATH 查找 */
        private String path;
    }
}
