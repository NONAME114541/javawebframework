package com.cdutetc.nanpo.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务（第13课定义）。
 * 只定义"存文件"这件事怎么做，不关心存在哪——
 * 本地磁盘（LocalStorageService，本课实现）、
 * MinIO 对象存储（MinioStorageService，第19课容器化时新增），
 * 业务代码面向本接口编程，换实现一行不改。
 */
public interface StorageService {

    /**
     * 保存文件，返回可访问的 URL 路径（如 /files/xxx.png）
     */
    String store(MultipartFile file);
}
