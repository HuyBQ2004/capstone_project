package com.sfms.common.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Trừu tượng lưu file nhị phân (ảnh hồ sơ, evidence photo, batch certificate...) — dùng
 * chung cho mọi module, không riêng MF01.
 *
 * <p>ADR-001 (hệ thống, §AI Prompt Constraint mục 6): production BẮT BUỘC lưu lên
 * <b>Cloudflare R2</b>, không lưu trong Docker volume của server. ADR-000 (dev companion):
 * môi trường local thay R2 bằng thư mục {@code ./data/uploads}.
 *
 * <p>Service nghiệp vụ (VD {@code ProfileService}) chỉ phụ thuộc interface này — khi hạ tầng
 * R2 sẵn sàng, thêm {@code R2StorageService implements StorageService} và đổi
 * {@code sfms.storage.provider=r2}, KHÔNG cần sửa bất kỳ service nghiệp vụ nào.
 */
public interface StorageService {

    /**
     * Lưu {@code file} dưới khóa {@code key} (VD {@code "avatars/U-0001.jpg"}).
     *
     * @return URL/đường dẫn <b>công khai</b> — chỉ dùng để lưu vào cột DB của tài liệu
     *         <b>CÔNG KHAI</b> (VD {@code avatar_url}). Tài liệu <b>RIÊNG TƯ</b> (D10-BIS mục 2,
     *         VD {@code partner_documents.file_key} của MF04) phải lưu {@code key} — tham số
     *         truyền vào — KHÔNG lưu giá trị trả về của hàm này, và đọc lại bằng {@link #load}.
     * @throws StorageException lưu thất bại (I/O, kết nối object storage...)
     */
    String store(String key, MultipartFile file);

    /**
     * Lưu file server tự sinh (không phải HTTP upload) — VD backup CSDL mã hoá
     * ({@code BackupServiceImpl}, BR-160/161). Cùng quy tắc riêng tư như {@link #store}: lưu
     * {@code key}, KHÔNG lưu URL trả về, cho tài liệu không công khai.
     */
    String store(String key, byte[] content, String contentType);

    /**
     * Đọc lại nội dung đã lưu ở {@code key} — dùng cho endpoint tải xuống tài liệu
     * <b>riêng tư</b> có {@code @PreAuthorize} (D10-BIS mục 2), KHÔNG đi qua CDN/URL công khai.
     *
     * @throws StorageException {@code key} không tồn tại hoặc đọc thất bại
     */
    Resource load(String key);
}
