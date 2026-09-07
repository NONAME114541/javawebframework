package com.cdutetc.nanpo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 文件存储配置。
 * 第13课：dir 外部化（换目录只改配置）。
 * 第19课：新增 type 选择实现（local/minio）+ MinIO 连接段——
 * docker-compose 里 NANPO_STORAGE_TYPE=minio 一行，就把第13课埋的暗线兑现了。
 */
@Component
@ConfigurationProperties(prefix = "nanpo.storage")
@Data
public class StorageProperties {

    /** 存储实现类型：local（本地磁盘，开发默认）/ minio（对象存储，容器化用） */
    private String type = "local";

    /** 本地存储目录（相对路径基于项目运行目录） */
    private String dir = "./upload";

    /** MinIO 对象存储配置（type=minio 时生效） */
    private final Minio minio = new Minio();

    @Data
    public static class Minio {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "nanpo";
        private String secretKey = "nanpo123";
        private String bucket = "nanpo";
    }
}
