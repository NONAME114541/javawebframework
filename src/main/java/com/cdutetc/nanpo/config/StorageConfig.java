package com.cdutetc.nanpo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cdutetc.nanpo.service.LocalStorageService;
import com.cdutetc.nanpo.service.MinioStorageService;
import com.cdutetc.nanpo.service.StorageService;

/**
 * 存储实现的装配车间（第19课）。
 * 按 nanpo.storage.type 选择把哪个实现注册为 Bean——
 * 本地开发 local（默认），docker-compose 里 NANPO_STORAGE_TYPE=minio 一行切换。
 * 这就是第4课"@Autowired 按类型要人"的完整闭环：接口不变、实现可换、装配集中一处。
 */
@Configuration
public class StorageConfig {

    @Bean
    public StorageService storageService(StorageProperties props) {
        if ("minio".equals(props.getType())) {
            return new MinioStorageService(props);
        }
        return new LocalStorageService(props);
    }
}
