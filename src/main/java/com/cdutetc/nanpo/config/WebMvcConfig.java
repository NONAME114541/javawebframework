package com.cdutetc.nanpo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cdutetc.nanpo.common.JwtInterceptor;

/**
 * Spring MVC 配置。
 * 第14课：曾把 /files/** 静态映射到本地上传目录。
 * 第16课：注册查票员（JwtInterceptor）——下单要先亮会员卡。
 * 第19课：静态映射退役——/files/{filename} 改由 KitchenController 端点代理
 *        （经 StorageService 抽象，本地磁盘/MinIO 通吃），本类只剩拦截器注册。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 本店规矩：逛菜单(/menu)、看照片(/files/**)、办卡登录随便，
        // 只有下单相关(/order/**)必须亮卡。
        // 反向思路（拓展）：接口多了以后，更常见的是 /** 全拦 + excludePathPatterns 白名单
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/order/**");
    }
}
