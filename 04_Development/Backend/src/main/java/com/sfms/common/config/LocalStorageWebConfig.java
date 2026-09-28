package com.sfms.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Phục vụ file đã lưu qua {@code LocalDiskStorageService} (ADR-000 — dev thay thế Cloudflare
 * R2 bằng thư mục {@code ./data/uploads}). Vô hiệu khi {@code sfms.storage.provider=r2}
 * (R2 phục vụ file trực tiếp qua CDN, backend không cần resource handler này).
 */
@Configuration
@ConditionalOnProperty(name = "sfms.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageWebConfig implements WebMvcConfigurer {

    private final String location;
    private final String publicBasePath;

    public LocalStorageWebConfig(
            @Value("${sfms.storage.local.base-dir:./data/uploads}") String baseDir,
            @Value("${sfms.storage.local.public-base-path:/media}") String publicBasePath) {
        this.location = Path.of(baseDir).toAbsolutePath().normalize().toUri().toString();
        this.publicBasePath = publicBasePath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(publicBasePath + "/**").addResourceLocations(location);
    }
}
