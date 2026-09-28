package com.sfms.mf12.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf12.dto.KnowledgeDocumentDtos.CreateKnowledgeDocumentRequest;
import com.sfms.mf12.dto.KnowledgeDocumentDtos.KnowledgeDocumentResponse;

import java.util.List;

/** Kho tri thức AI (System Admin) — nội dung active gửi kèm mỗi lượt gọi RAG qua {@link GeminiClient}. */
public interface KnowledgeDocumentService {

    List<KnowledgeDocumentResponse> list();

    KnowledgeDocumentResponse create(CreateKnowledgeDocumentRequest request, AuthUser actor);

    /**
     * Nạp tài liệu từ FILE (.pdf/.docx/.txt/.md/.csv) — bóc văn bản rồi đi tiếp đúng luồng
     * {@link #create}. Không lưu file gốc: kho tri thức cần văn bản cho ngữ cảnh RAG.
     */
    KnowledgeDocumentResponse createFromFile(org.springframework.web.multipart.MultipartFile file,
                                              String title, String category, AuthUser actor);

    /**
     * Cắt đoạn + sinh lại vector nhúng cho MỘT tài liệu (pgvector, V59).
     *
     * <p>Cần khi tài liệu được nạp lúc mô hình embedding đang tắt (đoạn lưu với
     * {@code embedding = NULL}), hoặc khi đổi mô hình embedding — vector của mô hình cũ và mới
     * KHÔNG so sánh được với nhau, để lẫn thì kết quả tìm kiếm sai mà không có lỗi nào báo ra.
     */
    KnowledgeDocumentResponse reindex(String code);

    KnowledgeDocumentResponse setActive(String code, boolean active, AuthUser actor);

    void delete(String code);

    /** Dùng bởi {@link GeminiClient}/{@link AiGatewayImpl} — nội dung thô, không qua DTO. */
    List<String> activeContents(int limit);

    /**
     * Đoạn tài liệu LIÊN QUAN tới câu hỏi, trong ngân sách ký tự — xem
     * {@link KnowledgeContextSelector}.
     *
     * <p>Thay cho {@link #activeContents(int)} ở luồng hỏi–đáp: giới hạn theo <i>số tài liệu</i>
     * là vô nghĩa khi một văn bản luật nặng 168KB.
     */
    List<String> contextFor(String prompt);
}
