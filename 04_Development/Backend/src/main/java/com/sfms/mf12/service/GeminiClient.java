package com.sfms.mf12.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client connector calling the Python FastAPI RAG Microservice (Gemini 2.0 Flash & LangChain)
 * running on port 8001 in Docker network.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    public static final String STUB_MODEL = "gemini-2.0-flash-stub";

    private final RestTemplate restTemplate;
    private final String ragServiceUrl;
    private final boolean enabled;

    public record GeminiResult(String responseText, String model) {}

    /*
     * CONSTRUCTOR DUY NHẤT — đừng thêm cái thứ hai (sửa 09/08/2026).
     *
     * Trước đây class này còn một constructor KHÔNG THAM SỐ cắm cứng "http://localhost:8001".
     * Spring gặp nhiều constructor mà không cái nào đánh dấu `@Autowired` thì chọn cái KHÔNG
     * THAM SỐ — nên `@Value` bên dưới CHƯA BAO GIỜ được áp, và `SFMS_RAG_URL` trong
     * docker-compose (`http://rag-service:8001`) bị bỏ qua hoàn toàn.
     *
     * Hệ quả đo được 08/08/2026: mọi câu hỏi cho trợ lý AI đều trả
     * "Đây là phản hồi mẫu (stub) cho: …" với model `gemini-2.0-flash-stub`, kèm log
     * "Connection refused" tới localhost:8001 — trong khi rag-service VẪN CHẠY và gọi được từ
     * chính container backend. Cấu hình đúng, container đúng, chỉ là code không đọc tới.
     *
     * Không ai dùng constructor rỗng đó (grep toàn repo: 0 nơi), nên đã xoá hẳn thay vì đánh
     * dấu `@Autowired` — một constructor thì không có gì để chọn nhầm.
     */
    public GeminiClient(
            RestTemplate restTemplate,
            @Value("${sfms.rag.url:http://rag-service:8001}") String ragServiceUrl,
            @Value("${sfms.rag.enabled:true}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.ragServiceUrl = ragServiceUrl;
        this.enabled = enabled;
    }


    /**
     * Sinh vector nhúng cho các đoạn văn bản — dùng cho truy hồi bằng pgvector (V59).
     *
     * <p>Đi qua rag-service chứ không gọi thẳng nhà cung cấp: service đó là nơi DUY NHẤT biết
     * đang chạy Gemini hay mô hình local (`SFMS_AI_PROVIDER`). Sinh embedding bằng một mô hình
     * mà truy vấn lại dùng mô hình khác thì kết quả tìm kiếm sai hoàn toàn nhưng KHÔNG có lỗi
     * nào báo ra — nên chỉ được có một nơi quyết định.
     *
     * @param task {@code "query"} cho câu hỏi, {@code "document"} cho đoạn tài liệu. KHÔNG phải
     *             tham số thừa: `nomic-embed-text` được huấn luyện với tiền tố nhiệm vụ riêng cho
     *             hai loại này, dùng sai thì truy hồi kém hẳn (đo 09/08/2026).
     * @return danh sách vector theo ĐÚNG thứ tự {@code texts}; rỗng khi không sinh được
     *         (mô hình embedding tắt/hỏng) — caller tự quyết định vẫn lưu hay báo lỗi.
     */
    @SuppressWarnings("unchecked")
    public List<List<Double>> embed(List<String> texts, String task) {
        if (!enabled || texts == null || texts.isEmpty()) {
            return List.of();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("texts", texts, "task", task), headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ragServiceUrl + "/api/v1/rag/embed", entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object vectors = response.getBody().get("vectors");
                if (vectors instanceof List<?> list && list.size() == texts.size()) {
                    return (List<List<Double>>) list;
                }
                // Lệch số lượng = gán nhầm vector cho đoạn. Thà không có embedding còn hơn có
                // embedding SAI: cái sai không báo lỗi, chỉ làm kết quả tìm kiếm vô nghĩa.
                log.error("rag-service trả {} vector cho {} đoạn — bỏ qua toàn bộ lô này",
                        vectors instanceof List<?> l ? l.size() : "?", texts.size());
            }
        } catch (Exception e) {
            log.error("Gọi {}/api/v1/rag/embed HỎNG: {}", ragServiceUrl, e.getMessage());
        }
        return List.of();
    }

    /** @throws AiTimeoutException khi prompt rỗng — mô phỏng lỗi kết nối cho test (UC-012 AF1). */
    public String complete(String prompt) {
        return complete(prompt, List.of());
    }

    public String complete(String prompt, List<String> contextDocs) {
        return completeWithModel(prompt, contextDocs).responseText();
    }

    /**
     * @param contextDocs Nội dung kho tri thức (System Admin quản trị, `KnowledgeDocumentService`) —
     *                     gửi kèm để RAG ưu tiên dùng thay vì tự khớp từ khoá trong KB tĩnh nội bộ.
     * @throws AiTimeoutException khi prompt rỗng — mô phỏng lỗi kết nối cho test (UC-012 AF1).
     */
    public GeminiResult completeWithModel(String prompt, List<String> contextDocs) {
        if (prompt == null || prompt.isBlank()) {
            throw new AiTimeoutException("Empty prompt");
        }

        if (enabled) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = new HashMap<>();
                body.put("prompt", prompt);
                body.put("module", "MF12_CHAT");
                if (contextDocs != null && !contextDocs.isEmpty()) {
                    body.put("context_docs", contextDocs);
                }

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        ragServiceUrl + "/api/v1/rag/chat",
                        entity,
                        Map.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String responseText = (String) response.getBody().get("response_text");
                    String modelName = (String) response.getBody().get("model");
                    if (responseText != null && !responseText.isBlank()) {
                        log.info("Received response from RAG Service successfully, model={}", modelName);
                        return new GeminiResult(responseText, modelName != null ? modelName : "gemini-2.0-flash");
                    }
                }
            } catch (Exception e) {
                log.warn("Call to RAG Service failed: {}. Falling back to canned response.", e.getMessage());
            }
        }

        return new GeminiResult(
                "Đây là phản hồi mẫu (stub) cho: " + prompt.substring(0, Math.min(prompt.length(), 120)),
                STUB_MODEL
        );
    }

    public static class AiTimeoutException extends RuntimeException {
        public AiTimeoutException(String message) {
            super(message);
        }
    }
}
