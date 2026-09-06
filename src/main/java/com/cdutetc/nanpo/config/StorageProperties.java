package com.cdutetc.nanpo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 文件存储配置（第13课）。
 * @ConfigurationProperties：把 yml 里 nanpo.storage.* 绑定成强类型对象——
 * 换存储目录只改配置文件，不用动代码（第19课换 MinIO 时同理只动配置/实现）。
 */
@Component
@ConfigurationProperties(prefix = "nanpo.storage")
@Data
public class StorageProperties {

    /** 本地存储目录（相对路径基于项目运行目录） */
    private String dir = "./upload";
}
