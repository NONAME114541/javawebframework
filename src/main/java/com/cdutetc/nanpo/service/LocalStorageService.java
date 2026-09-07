package com.cdutetc.nanpo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.config.StorageProperties;

/**
 * 本地磁盘存储实现（第13课）。
 * 第19课调整：去掉 @Service——实例改由 config/StorageConfig 工厂方法
 * 按 nanpo.storage.type 选择装配（另一个候选：MinioStorageService）；
 * 校验+改名逻辑上移到接口的 default 方法；新增 load() 供端点代理读取。
 * 两个历史坑仍防着：
 * 1. transferTo(相对路径) 会存到 multipart 临时目录——rootDir 一律转绝对路径；
 * 2. 目录可能不存在——启动时 createDirectories。
 */
public class LocalStorageService implements StorageService {

    private final Path rootDir;

    public LocalStorageService(StorageProperties props) {
        // 转绝对路径并规范化，防 transferTo 相对路径坑
        this.rootDir = Path.of(props.getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建上传目录：" + rootDir, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String filename = validateAndRename(file);   // 接口 default 方法：校验+UUID 改名
        try {
            file.transferTo(rootDir.resolve(filename));
        } catch (IOException e) {
            throw new IllegalStateException("保存文件失败：" + filename, e);
        }
        return "/files/" + filename;
    }

    @Override
    public Resource load(String filename) {
        // 防路径穿越：解析后的路径必须还在上传目录内（第14课的老规矩搬进实现层）
        Path path = rootDir.resolve(filename).normalize();
        if (!path.startsWith(rootDir) || !Files.exists(path)) {
            throw new BusinessException(ErrorCodeEnum.FILE_NOT_FOUND);
        }
        return new FileSystemResource(path);
    }
}
