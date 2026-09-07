package com.cdutetc.nanpo.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.config.StorageProperties;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

/**
 * MinIO 对象存储实现（第19课）——第13课埋的暗线在此兑现：
 * KitchenController 注入的还是 StorageService 接口，业务代码一行未改；
 * 激活方式：nanpo.storage.type=minio（开发环境默认 local，互不干扰）。
 * 设计要点：store() 仍返回 /files/xxx.png 短路径，图片访问走应用端点代理——
 * 浏览器不感知 MinIO 的内网地址（预签名 URL 会暴露容器拓扑，且长达数百字符），
 * 将来加鉴权/换 CDN 也只动这一个类。
 */
public class MinioStorageService implements StorageService {

    private final MinioClient client;
    private final String bucket;

    public MinioStorageService(StorageProperties props) {
        StorageProperties.Minio minio = props.getMinio();
        this.client = MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();
        this.bucket = minio.getBucket();
        try {
            // 桶不存在就建——幂等思想，和第8课菜单初始化同一招
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 初始化失败（检查端点与密钥）：" + e.getMessage(), e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String filename = validateAndRename(file);   // 与本地实现共用接口 default 方法
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(filename)
                    .stream(file.getInputStream(), file.getSize(), -1L)
                    .contentType(file.getContentType())
                    .build());
            return "/files/" + filename;   // 短路径——真实地址由端点代理屏蔽
        } catch (Exception e) {
            throw new IllegalStateException("上传 MinIO 失败：" + filename, e);
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            return new InputStreamResource(
                    client.getObject(GetObjectArgs.builder().bucket(bucket).object(filename).build()));
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.FILE_NOT_FOUND);
        }
    }
}
