package com.sfms.mf11.service;

import org.springframework.core.io.Resource;

/**
 * Kèm {@code fileName} gốc (11/08/2026) — cùng lớp lỗi đã vá ở {@code mf07.EvidenceService}/
 * {@code mf08.DownloadedFile}: trước đây {@code Mf11AttachmentSupport.download()} chỉ trả
 * {@link Resource} trần, controller không có gì để suy MIME type nên set
 * {@code Content-Type: application/octet-stream} + {@code Content-Disposition: attachment},
 * khiến ảnh bằng chứng sự cố/CAPA/báo cáo nguồn mở tab mới ra chuỗi byte nhị phân.
 */
public record DownloadedFile(Resource resource, String fileName) {
}
