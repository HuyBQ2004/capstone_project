package com.sfms.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implementation mặc định — ADR-000: dev/local lưu vào thư mục {@code ./data/uploads} thay
 * vì Cloudflare R2. Mặc định khi CHƯA cấu hình {@code sfms.storage.provider} (hoặc đặt
 * {@code =local}), nên hoạt động ngay cả khi chưa có R2 credentials.
 */
@Service
@ConditionalOnProperty(name = "sfms.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalDiskStorageService implements StorageService {

    private final Path baseDir;
    private final String publicBasePath;

    public LocalDiskStorageService(
            @Value("${sfms.storage.local.base-dir:./data/uploads}") String baseDir,
            @Value("${sfms.storage.local.public-base-path:/media}") String publicBasePath) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
        this.publicBasePath = publicBasePath;
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new StorageException("Không tạo được thư mục lưu file: " + this.baseDir, e);
        }
    }

    @Override
    public String store(String key, MultipartFile file) {
        Path target = resolveSafe(key);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new StorageException("Không ghi được file: " + key, e);
        }
        return publicBasePath + "/" + key;
    }

    @Override
    public String store(String key, byte[] content, String contentType) {
        Path target = resolveSafe(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException e) {
            throw new StorageException("Không ghi được file: " + key, e);
        }
        return publicBasePath + "/" + key;
    }

    @Override
    public Resource load(String key) {
        Path target = resolveSafe(key);
        if (!Files.isReadable(target)) {
            throw new StorageException("Không đọc được file: " + key);
        }
        return new FileSystemResource(target);
    }

    /** Chặn path traversal — {@code key} luôn phải giải quyết bên trong {@link #baseDir}. */
    private Path resolveSafe(String key) {
        Path target = baseDir.resolve(key).normalize();
        if (!target.startsWith(baseDir)) {
            throw new StorageException("Khóa file không hợp lệ: " + key);
        }
        return target;
    }
}
