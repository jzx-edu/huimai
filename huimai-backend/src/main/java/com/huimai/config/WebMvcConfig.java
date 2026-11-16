package com.huimai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：跨域、本地媒体资源静态映射。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${huimai.storage.root}")
    private String storageRoot;

    @Value("${huimai.resource.prefix}")
    private String resourcePrefix;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = storageRoot.endsWith("/") ? storageRoot : storageRoot + "/";
        registry.addResourceHandler(resourcePrefix)
                .addResourceLocations("file:" + location);
    }
}
