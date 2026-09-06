package com.cdutetc.nanpo.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置（第14课）。
 * 项目里第一个正式的 @Configuration 类——把 /files/** 映射到本地上传目录，
 * 浏览器就能直接访问照片（第1课讲过的静态资源约定的"手动版"）。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    public WebMvcConfig(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 坑：目录型 resource location 必须以 / 结尾，否则映射不生效
        String location = Path.of(storageProperties.getDir())
                .toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
