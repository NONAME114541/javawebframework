package com.cdutetc.nanpo.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.entity.Menu;
import com.cdutetc.nanpo.service.StorageService;

/**
 * 后厨管理（第13课新增）：菜品照片上架——经理/后厨用的接口，
 * 与服务顾客的 WaiterController 并存，一个店多个"窗口"。
 * 第14课新增：照片下载接口（Content-Disposition 让浏览器"另存为"）。
 * 第19课升级：本类不再直接碰磁盘（原来 download 里还有 Path/Files）——
 * 上传、下载、访问全部走 StorageService 抽象，本地/MinIO 由配置决定。
 */
@RestController
public class KitchenController {

    @Autowired
    private MenuRepository menuRepository;

    // 面向接口编程：注入的是 StorageService 而不是某个实现——
    // 第19课换 MinIO 后，这个类一行都没改
    @Autowired
    private StorageService storageService;

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

    // 图片访问端点（第19课）：统一由应用代理——本地实现读磁盘、MinIO 实现拉对象流，
    // 浏览器不感知存储拓扑。接替第14课 WebMvcConfig 的静态映射（它只认本地目录，
    // 存储搬进容器后不再够用）；前端 <img src="/files/xxx.png"> 的用法完全不变
    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource resource = storageService.load(filename);
        String contentType = java.net.URLConnection.guessContentTypeFromName(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    // 下载文件：和 /files/{filename} 直接访问的区别——
    // Content-Disposition: attachment 让浏览器弹出"另存为"而不是直接打开
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        Resource resource = storageService.load(filename);   // 读取也走抽象（第19课）
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
