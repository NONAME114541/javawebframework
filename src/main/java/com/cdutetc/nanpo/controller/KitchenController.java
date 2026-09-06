package com.cdutetc.nanpo.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.config.StorageProperties;
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.entity.Menu;
import com.cdutetc.nanpo.service.StorageService;

/**
 * 后厨管理（第13课新增）：菜品照片上架——经理/后厨用的接口，
 * 与服务顾客的 WaiterController 并存，一个店多个"窗口"。
 * 第14课新增：照片下载接口（Content-Disposition 让浏览器"另存为"）。
 */
@RestController
public class KitchenController {

    @Autowired
    private MenuRepository menuRepository;

    // 面向接口编程：注入的是 StorageService 而不是 LocalStorageService——
    // 第19课新增 MinIO 实现后，这里一行都不用改
    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageProperties storageProperties;

    // 上架菜品照片：multipart/form-data，文件字段名 file
    @PostMapping("/menu/{id}/image")
    public Result<String> uploadImage(@PathVariable Long id,
                                      @RequestParam("file") MultipartFile file) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.DISH_NOT_FOUND));

        String url = storageService.store(file);   // 存文件，拿回访问路径
        menu.setImageUrl(url);                     // 记到菜单上
        menuRepository.save(menu);
        return Result.success(url);
    }

    // 下载文件：和 /files/** 直接访问的区别——
    // Content-Disposition: attachment 让浏览器弹出"另存为"而不是直接打开
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        Path root = Path.of(storageProperties.getDir()).toAbsolutePath().normalize();
        Path path = root.resolve(filename).normalize();

        // 防路径穿越：/download/../pom.xml 之类必须被拦下——
        // 解析后的路径必须仍然位于上传目录内
        if (!path.startsWith(root) || !Files.exists(path)) {
            throw new BusinessException(ErrorCodeEnum.FILE_NOT_FOUND);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(path));
    }
}
