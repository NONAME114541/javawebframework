package com.cdutetc.nanpo.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cdutetc.nanpo.common.JwtInterceptor;

/**
 * Spring MVC 配置。
 * 第14课：把 /files/** 映射到本地上传目录（照片直接被浏览器访问）。
 * 第16课：注册查票员（JwtInterceptor）——下单要先亮会员卡。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Autowired
    private JwtInterceptor jwtInterceptor;

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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 本店规矩：逛菜单(/menu)、看照片(/files/**)、办卡登录随便，
        // 只有下单相关(/order/**)必须亮卡。
        // 反向思路（口头）：接口多了以后，更常见的是 /** 全拦 + excludePathPatterns 白名单
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/order/**");
    }
}
