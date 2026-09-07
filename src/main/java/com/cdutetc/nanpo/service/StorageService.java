package com.cdutetc.nanpo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;

/**
 * 文件存储服务（第13课定义）。
 * 只定义"存文件"这件事怎么做，不关心存在哪——
 * 本地磁盘（LocalStorageService）与 MinIO 对象存储（MinioStorageService，第19课）
 * 是它的两个实现，业务代码面向本接口编程，换实现一行不改。
 * 第19课：公共的校验+改名逻辑提取为 default 方法；
 *         新增 load()——图片访问统一走应用端点 /files/{filename}，
 *         由实现类决定从磁盘还是对象存储取流，浏览器不感知存储拓扑。
 */
public interface StorageService {

    /**
     * 保存文件，返回可访问的 URL 路径（/files/xxx.png）
     */
    String store(MultipartFile file);

    /**
     * 按文件名读取文件（供 /files/{filename} 与 /download/{filename} 端点代理访问）
     */
    Resource load(String filename);

    /**
     * 公共校验与改名（第19课提取）：空文件/扩展名白名单校验，返回 UUID 新文件名。
     * default 方法——接口里带实现，两个实现类直接继承用。
     */
    default String validateAndRename(MultipartFile file) {
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
        if (!List.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(ext)) {
            throw new BusinessException(ErrorCodeEnum.FILE_INVALID);
        }
        // 重名防护：UUID 做新文件名，原文件名只留扩展名
        return UUID.randomUUID() + ext;
    }
}
