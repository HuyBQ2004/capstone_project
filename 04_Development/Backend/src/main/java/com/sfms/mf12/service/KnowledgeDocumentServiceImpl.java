package com.sfms.mf12.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf12.domain.KnowledgeCategory;
import com.sfms.mf12.domain.KnowledgeDocument;
import com.sfms.mf12.dto.KnowledgeDocumentDtos.CreateKnowledgeDocumentRequest;
import com.sfms.mf12.dto.KnowledgeDocumentDtos.KnowledgeDocumentResponse;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.repository.KnowledgeChunkRepository;
import com.sfms.mf12.repository.KnowledgeDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Year;
import java.util.List;

@Service
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeDocumentServiceImpl.class);

    /**
     * Số đoạn lấy về từ tìm kiếm vector trước khi cắt theo ngân sách ký tự. 12 đoạn × ~1200 ký
     * tự là dư cho ngân sách 6000, nên phần thừa bị loại ở vòng lọc — lấy dư một chút để đoạn
     * quá dài không làm mất luôn các đoạn liên quan xếp sau nó.
     */
    private static final int VECTOR_TOP_K = 12;

    private final KnowledgeDocumentRepository repository;
    private final KnowledgeChunkRepository chunkRepository;
    private final GeminiClient geminiClient;
    private final CodeGenerator codeGenerator;
    private final KnowledgeTextExtractor textExtractor;
    private final KnowledgeContextSelector contextSelector;
    private final Clock clock;

    public KnowledgeDocumentServiceImpl(KnowledgeDocumentRepository repository,
                                         KnowledgeChunkRepository chunkRepository,
                                         GeminiClient geminiClient, CodeGenerator codeGenerator,
                                         KnowledgeTextExtractor textExtractor,
                                         KnowledgeContextSelector contextSelector, Clock clock) {
        this.repository = repository;
        this.chunkRepository = chunkRepository;
        this.geminiClient = geminiClient;
        this.codeGenerator = codeGenerator;
        this.textExtractor = textExtractor;
        this.contextSelector = contextSelector;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeDocumentResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public KnowledgeDocumentResponse create(CreateKnowledgeDocumentRequest request, AuthUser actor) {
        KnowledgeCategory category = parseCategory(request.category());

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setCode(codeGenerator.next("mf12_ai_knowledge:" + Year.now(clock).getValue(),
                "AIK-" + Year.now(clock).getValue(), 4));
        doc.setTitle(request.title());
        doc.setCategory(category);
        doc.setContent(request.content());
        doc.setActive(true);
        repository.saveAndFlush(doc);
        reindex(doc);
        return toResponse(doc);
    }

    /**
     * Cắt tài liệu thành đoạn, sinh vector nhúng, lưu vào {@code ai_knowledge_chunks} (V59).
     *
     * <p>Cắt sẵn LÚC NẠP chứ không lúc truy vấn: mỗi câu hỏi mà phải cắt lại 333.000 ký tự là
     * lãng phí, và quan trọng hơn — vector chỉ sinh được cho đoạn đã cố định.
     *
     * <p><b>Không chặn việc nạp khi embedding hỏng.</b> Mô hình embedding tắt thì đoạn vẫn được
     * lưu với {@code embedding = NULL}; tài liệu vẫn dùng được qua truy hồi từ khoá. Từ chối nạp
     * chỉ vì một phụ thuộc phụ đang tắt là đánh đổi sai — nhưng phải ghi log ERROR để còn biết
     * mà chạy lại, không im lặng.
     */
    private void reindex(KnowledgeDocument doc) {
        chunkRepository.deleteByDocumentId(doc.getId());
        List<String> chunks = contextSelector.chunkForIndexing(doc.getContent());
        if (chunks.isEmpty()) {
            return;
        }
        List<List<Double>> vectors = geminiClient.embed(chunks, "document");
        if (vectors.isEmpty()) {
            log.error("Nạp {} với {} đoạn nhưng CHƯA sinh được embedding — vẫn lưu, tìm kiếm sẽ "
                    + "rơi về truy hồi từ khoá. Chạy lại sau bằng POST /ai/knowledge-documents/{}/reindex",
                    doc.getCode(), chunks.size(), doc.getCode());
        }
        chunkRepository.insertChunks(doc.getId(), chunks, vectors);
    }

    @Override
    @Transactional
    public KnowledgeDocumentResponse reindex(String code) {
        KnowledgeDocument doc = repository.findByCode(code).orElseThrow(AisExceptions::notFound);
        reindex(doc);
        return toResponse(doc);
    }

    @Override
    @Transactional
    public KnowledgeDocumentResponse createFromFile(org.springframework.web.multipart.MultipartFile file,
                                                     String title, String category, AuthUser actor) {
        String content = textExtractor.extract(file);
        // Bỏ trống tiêu đề thì lấy TÊN FILE (bỏ đuôi) — nạp một lúc nhiều văn bản quy định thì
        // gõ lại từng tiêu đề là việc thừa, mà để trống lại vi phạm @NotBlank.
        String finalTitle = (title == null || title.isBlank()) ? fileNameAsTitle(file) : title.trim();
        return create(new CreateKnowledgeDocumentRequest(finalTitle, category, content), actor);
    }

    private String fileNameAsTitle(org.springframework.web.multipart.MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = name.lastIndexOf('.');
        String base = (dot > 0 ? name.substring(0, dot) : name).trim();
        return base.isBlank() ? "Tài liệu tri thức" : base;
    }

    @Override
    @Transactional
    public KnowledgeDocumentResponse setActive(String code, boolean active, AuthUser actor) {
        KnowledgeDocument doc = repository.findByCode(code).orElseThrow(AisExceptions::notFound);
        doc.setActive(active);
        repository.saveAndFlush(doc);
        return toResponse(doc);
    }

    @Override
    @Transactional
    public void delete(String code) {
        KnowledgeDocument doc = repository.findByCode(code).orElseThrow(AisExceptions::notFound);
        repository.delete(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> activeContents(int limit) {
        return repository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .limit(limit)
                .map(KnowledgeDocument::getContent)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> contextFor(String prompt) {
        /*
         * TRUY HỒI LAI (hybrid) — dùng CẢ HAI đường, mỗi đường nửa ngân sách.
         *
         * Vì sao không chọn một: đo 09/08/2026 trên chính kho tri thức này, hai đường mạnh yếu ở
         * hai loại câu hỏi khác nhau và không cái nào thắng tuyệt đối.
         *   · VECTOR hiểu câu diễn đạt khác chữ trong luật ("cả lớp đau bụng sau bữa trưa" ↔
         *     "ngộ độc thực phẩm tập thể") — từ khoá bó tay.
         *   · TỪ KHOÁ bám đúng thuật ngữ hiếm ("kiểm thực ba bước") — vector từng trả về nội dung
         *     kiểm tra hàng NHẬP KHẨU cho chính câu này.
         * Gộp lại thì mỗi loại câu hỏi đều có đường sống, đổi lại mỗi đường chỉ còn nửa ngân sách.
         */
        int halfBudget = KnowledgeContextSelector.DEFAULT_BUDGET_CHARS / 2;
        List<String> picked = new java.util.ArrayList<>();
        int used = 0;

        if (chunkRepository.countEmbedded() > 0) {
            List<List<Double>> queryVector = geminiClient.embed(List.of(prompt), "query");
            if (!queryVector.isEmpty()) {
                for (KnowledgeChunkRepository.ScoredChunk hit :
                        chunkRepository.searchNearest(queryVector.get(0), VECTOR_TOP_K)) {
                    if (used + hit.content().length() > halfBudget) {
                        continue;
                    }
                    picked.add(hit.content());
                    used += hit.content().length();
                }
            } else {
                log.warn("Có embedding trong DB nhưng không sinh được vector cho câu hỏi — "
                        + "lượt này chỉ còn truy hồi từ khoá.");
            }
        }

        /*
         * Nhánh TỪ KHOÁ. Giữ lại có chủ đích, không xoá: nó là đường sống khi mô hình embedding
         * tắt, khi tài liệu vừa nạp mà chưa kịp sinh vector, và khi ai đó chạy hệ thống không có
         * nhà cung cấp AI nào. Đọc HẾT tài liệu đang bật rồi mới chọn đoạn — giới hạn ở tầng SQL
         * sẽ loại nhầm đúng văn bản chứa câu trả lời.
         */
        List<String> all = repository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(KnowledgeDocument::getContent)
                .toList();
        int remaining = KnowledgeContextSelector.DEFAULT_BUDGET_CHARS - used;
        for (String chunk : contextSelector.select(prompt, all, remaining)) {
            // Hai đường rất hay chọn trùng đoạn — trùng thì vừa tốn ngân sách vừa làm model
            // tưởng ý đó quan trọng gấp đôi.
            if (!picked.contains(chunk)) {
                picked.add(chunk);
            }
        }
        return picked;
    }

    private static KnowledgeCategory parseCategory(String raw) {
        try {
            return KnowledgeCategory.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw AisExceptions.validationFailed("category", "must be one of ATTP_REGULATION/NUTRITION_STANDARD/SCHOOL_POLICY/OTHER");
        }
    }

    private KnowledgeDocumentResponse toResponse(KnowledgeDocument d) {
        return new KnowledgeDocumentResponse(d.getCode(), d.getTitle(), d.getCategory().name(), d.getContent(),
                d.isActive(), d.getCreatedAt(), d.getCreatedBy());
    }
}
