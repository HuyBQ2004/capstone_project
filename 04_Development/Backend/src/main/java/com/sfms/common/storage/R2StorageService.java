package com.sfms.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;

/**
 * ADR-001 §AI Prompt Constraint mục 6: production BẮT BUỘC lưu file lên Cloudflare R2, server
 * chỉ giữ đường dẫn trong PostgreSQL. R2 tương thích S3 API nên dùng thẳng AWS SDK v2, chỉ
 * đổi {@code endpointOverride} + bật path-style access (R2 yêu cầu path-style).
 *
 * <p>Kích hoạt bằng {@code sfms.storage.provider=r2}; mặc định vẫn là
 * {@link LocalDiskStorageService} (ADR-000, dev). Credentials/endpoint/bucket đọc từ ENV —
 * TUYỆT ĐỐI không hardcode (ADR-001 mục 2, ADR-000 mục 2).
 */
@Service
@ConditionalOnProperty(name = "sfms.storage.provider", havingValue = "r2")
public class R2StorageService implements StorageService {

    private final S3Client s3;
    private final String bucket;
    private final String publicBaseUrl;

    public R2StorageService(
            @Value("${sfms.storage.r2.endpoint}") String endpoint,
            @Value("${sfms.storage.r2.access-key}") String accessKey,
            @Value("${sfms.storage.r2.secret-key}") String secretKey,
            @Value("${sfms.storage.r2.bucket}") String bucket,
            @Value("${sfms.storage.r2.public-base-url}") String publicBaseUrl) {
        this.bucket = bucket;
        // bỏ dấu "/" cuối để nối key không bị "//"
        this.publicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
        this.s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                // R2 không có khái niệm region như AWS — "auto" là giá trị chuẩn Cloudflare khuyến nghị
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                // R2 yêu cầu path-style (bucket.r2.cloudflarestorage.com KHÔNG hỗ trợ virtual-hosted-style)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Override
    public String store(String key, MultipartFile file) {
        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                            file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new StorageException("Không đọc được nội dung file: " + key, e);
        } catch (S3Exception e) {
            throw new StorageException("R2 từ chối upload: " + key, e);
        }
        return publicBaseUrl + "/" + key;
    }

    @Override
    public String store(String key, byte[] content, String contentType) {
        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .contentLength((long) content.length)
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(content));
        } catch (S3Exception e) {
            throw new StorageException("R2 từ chối upload: " + key, e);
        }
        return publicBaseUrl + "/" + key;
    }

    @Override
    public Resource load(String key) {
        try {
            var object = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
            return new InputStreamResource(object);
        } catch (NoSuchKeyException e) {
            throw new StorageException("Không tìm thấy file trên R2: " + key, e);
        } catch (S3Exception e) {
            throw new StorageException("R2 từ chối tải xuống: " + key, e);
        }
    }
}
