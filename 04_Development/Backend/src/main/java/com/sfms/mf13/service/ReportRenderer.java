package com.sfms.mf13.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * BR-178 — renderer bắt buộc 4 field header (tiêu đề, khoảng thời gian, tên trường, timestamp
 * ISO). BR-181 — watermark {@code Confidential}. Thiếu {@code schoolName} (hoặc bất kỳ field
 * bắt buộc nào) PHẢI ném {@link IllegalArgumentException} — không render thiếu (fail-closed).
 */
public interface ReportRenderer {

    byte[] render(String title, String periodLabel, String schoolName, Instant generatedAt,
                  List<Map<String, Object>> rows);

    String contentType();

    String fileExtension();
}
