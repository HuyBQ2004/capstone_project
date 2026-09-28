package com.sfms.mf08.service;

import org.springframework.core.io.Resource;

/**
 * Kèm {@code fileName} gốc (11/08/2026) — dùng chung cho {@code FoodSampleService}/
 * {@code SampleLabRequestService}. Trước đây cả hai chỉ trả {@link Resource} trần, controller
 * không có gì để suy MIME type nên set {@code Content-Type: application/octet-stream} +
 * {@code Content-Disposition: attachment}, khiến ảnh bằng chứng mở tab mới ra chuỗi byte nhị
 * phân thay vì hiển thị — cùng lớp lỗi đã vá ở {@code EvidenceService}/{@code InspectionController}
 * (mf07) ngày 10/08/2026.
 */
public record DownloadedFile(Resource resource, String fileName) {
}
