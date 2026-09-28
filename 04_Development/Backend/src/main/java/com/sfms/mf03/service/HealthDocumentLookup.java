package com.sfms.mf03.service;

import com.sfms.mf03.domain.HealthDocument;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.HealthDocumentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Tra cứu tài liệu y tế đính kèm theo MÃ NGHIỆP VỤ (D1) — dùng chung cho 3 nơi nhận
 * {@code proofFileId}/{@code documentId} từ client: khai báo dị ứng, khai báo bệnh lý nền,
 * yêu cầu chế độ ăn đặc biệt.
 *
 * <p>Gom vào một chỗ vì phần QUAN TRỌNG không phải việc tra mã mà là {@link #requireOwnedBy}:
 * client tự gửi mã tài liệu, nên nếu không đối chiếu tài liệu có thuộc đúng học sinh đang khai
 * báo hay không thì bất kỳ phụ huynh nào cũng gắn được tài liệu y tế của học sinh khác vào hồ sơ
 * con mình — và Y tế sẽ mở đúng file đó khi duyệt (BR-031, C2). Ba nơi gọi phải cùng một luật.
 */
@Component
public class HealthDocumentLookup {

    private final HealthDocumentRepository repository;

    public HealthDocumentLookup(HealthDocumentRepository repository) {
        this.repository = repository;
    }

    /**
     * @param code      mã tài liệu client gửi lên ({@code HDOC-2026-0001}); {@code null}/rỗng ⇒ empty
     * @param studentId học sinh của bản ghi đang tạo — tài liệu phải thuộc đúng học sinh này
     * @throws com.sfms.common.error.ApiException STU-404 mã không tồn tại · STU-005 tài liệu của HS khác
     */
    public Optional<HealthDocument> requireOwnedBy(String code, UUID studentId) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        HealthDocument document = repository.findByCode(code).orElseThrow(StuExceptions::notFound);
        if (!document.getStudentId().equals(studentId)) {
            throw StuExceptions.accessDenied();
        }
        return Optional.of(document);
    }

    /** Đọc lại tài liệu đã lưu ở bản ghi (khoá UUID) để lấy mã + tên file cho tầng response. */
    public Optional<HealthDocument> byId(UUID id) {
        return id == null ? Optional.empty() : repository.findById(id);
    }
}
