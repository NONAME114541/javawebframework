package com.cdutetc.nanpo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.config.StorageProperties;

/**
 * 本地磁盘存储实现（第13课）。
 * 两个真实坑都在构造器里防了：
 * 1. transferTo(相对路径) 会存到 multipart 临时目录——rootDir 一律转绝对路径；
 * 2. 目录可能不存在——启动时 createDirectories。
 */
@Service
public class LocalStorageService implements StorageService {

    private static final List<String> ALLOWED_EXT = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final Path rootDir;

    public LocalStorageService(StorageProperties props) {
        // 转绝对路径并规范化，防 transferTo 相对路径坑 + 后面做路径穿越校验
        this.rootDir = Path.of(props.getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建上传目录：" + rootDir, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        // 校验一：空文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCodeEnum.FILE_INVALID);
        }
        // 校验二：扩展名白名单（教学版按扩展名判；生产要校验文件头/内容类型）
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.lastIndexOf('.') >= 0) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ErrorCodeEnum.FILE_INVALID);
        }

        // 重名防护：UUID 做新文件名，原文件名只留扩展名
        String filename = UUID.randomUUID() + ext;
        try {
            file.transferTo(rootDir.resolve(filename));
        } catch (IOException e) {
            throw new IllegalStateException("保存文件失败：" + filename, e);
        }
        // 返回可访问路径（/files/** 的映射第14课才配——那正是本课的钩子）
        return "/files/" + filename;
    }
}
