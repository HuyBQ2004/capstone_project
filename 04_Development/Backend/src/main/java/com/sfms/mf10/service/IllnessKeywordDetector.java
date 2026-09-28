package com.sfms.mf10.service;

import com.sfms.mf10.repository.IllnessKeywordRepository;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * BR-142 (ADR-M10-6) — phát hiện từ khóa bệnh lý trong nội dung phản hồi. So khớp
 * KHÔNG dấu, KHÔNG phân biệt hoa/thường ở TẦNG JAVA ({@link Normalizer}) — V18 KHÔNG bật
 * extension {@code unaccent} của PostgreSQL (simplification ghi ở V18, giữ hạ tầng DB gọn).
 */
@Component
public class IllnessKeywordDetector {

    private final IllnessKeywordRepository repository;

    public IllnessKeywordDetector(IllnessKeywordRepository repository) {
        this.repository = repository;
    }

    /** @return {@code true} nếu {@code text} chứa >=1 từ khóa bệnh lý ĐANG active. */
    public boolean detect(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalizedText = normalize(text);
        List<String> keywords = repository.findByActiveTrue().stream()
                .map(k -> normalize(k.getKeyword())).toList();
        return keywords.stream().anyMatch(normalizedText::contains);
    }

    /** Bỏ dấu tiếng Việt + hạ chữ thường — không thay đổi hành vi khi input đã là ASCII. */
    static String normalize(String raw) {
        String decomposed = Normalizer.normalize(raw, Normalizer.Form.NFD);
        String stripped = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(decomposed).replaceAll("");
        return stripped.replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }
}
