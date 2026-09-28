package com.sfms.mf12.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf12.dto.AiDtos.AiChatResponse;
import com.sfms.mf12.dto.AiDtos.AiConversationDetail;
import com.sfms.mf12.dto.AiDtos.AiConversationSummary;
import com.sfms.mf12.dto.AiDtos.AskRequest;
import com.sfms.mf12.dto.KnowledgeDocumentDtos.CreateKnowledgeDocumentRequest;
import com.sfms.mf12.dto.KnowledgeDocumentDtos.KnowledgeDocumentResponse;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.service.AiChatService;
import com.sfms.mf12.service.KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** MF12 — {@code ai} (UC-012/UC-119). BR-156/BR-157. */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiChatService aiChatService;
    private final KnowledgeDocumentService knowledgeDocumentService;

    public AiController(AiChatService aiChatService, KnowledgeDocumentService knowledgeDocumentService) {
        this.aiChatService = aiChatService;
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    @Operation(summary = "Chat với AI", description = "UC-012 · BR-157 — archive bắt buộc cùng transaction")
    @ApiResponse(responseCode = "503", description = "AIS-073 — MSG-073",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @PostMapping("/chat")
    public AiChatResponse chat(@Valid @RequestBody AskRequest request, @CurrentUser AuthUser actor) {
        return aiChatService.ask(request, actor.userId());
    }

    @Operation(summary = "Lịch sử hội thoại AI", description = "UC-119 · BR-157 — chỉ hội thoại của chính mình")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping("/conversations")
    public PagedResponse<AiConversationSummary> conversations(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "8") int pageSize,
                                                               @CurrentUser AuthUser actor) {
        return aiChatService.history(page, pageSize, actor.userId());
    }

    @Operation(summary = "Chi tiết một hội thoại AI", description = "UC-119")
    @ApiResponse(responseCode = "404", description = "AIS-404",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping("/conversations/{code}")
    public AiConversationDetail conversation(@PathVariable String code, @CurrentUser AuthUser actor) {
        return aiChatService.conversation(code, actor.userId()).orElseThrow(AisExceptions::notFound);
    }

    @Operation(summary = "Danh sách kho tri thức AI", description = "System Admin quản trị — nội dung " +
            "active được gửi kèm mỗi lượt gọi RAG (context_docs)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/knowledge-documents")
    public List<KnowledgeDocumentResponse> knowledgeDocuments() {
        return knowledgeDocumentService.list();
    }

    @Operation(summary = "Thêm tài liệu tri thức", description = "System Admin — đoạn văn bản quy định/" +
            "hướng dẫn dùng làm ngữ cảnh RAG")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping("/knowledge-documents")
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeDocumentResponse createKnowledgeDocument(@Valid @RequestBody CreateKnowledgeDocumentRequest request,
                                                              @CurrentUser AuthUser actor) {
        return knowledgeDocumentService.create(request, actor);
    }

    /*
     * Nạp bằng FILE thay vì dán tay. Kho tri thức rỗng làm hỏng chính tính năng nó phục vụ:
     * `SYSTEM_PROMPT` buộc trợ lý chỉ dựa trên tài liệu tham chiếu, nên không có tài liệu thì
     * mô hình nhỏ TỪ CHỐI trả lời câu nghiệp vụ (đo 09/08/2026 với gemma-4-e4b chạy local).
     * Trước đó muốn nạp Nghị định 15/2018 thì phải mở file, bôi đen, dán vào Textarea.
     */
    @Operation(summary = "Nạp tài liệu tri thức từ FILE",
            description = "System Admin — .pdf/.docx/.txt/.md/.csv ≤10MB. Server bóc văn bản (PDFBox/POI), "
                    + "KHÔNG lưu file gốc. Bỏ trống `title` thì lấy tên file.")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping(value = "/knowledge-documents/upload",
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeDocumentResponse uploadKnowledgeDocument(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false, defaultValue = "OTHER") String category,
            @CurrentUser AuthUser actor) {
        return knowledgeDocumentService.createFromFile(file, title, category, actor);
    }

    @Operation(summary = "Sinh lại vector nhúng cho một tài liệu",
            description = "Dùng khi tài liệu được nạp lúc mô hình embedding đang tắt, hoặc sau khi đổi mô hình "
                    + "embedding (vector của hai mô hình KHÔNG so sánh được với nhau)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping("/knowledge-documents/{code}/reindex")
    public KnowledgeDocumentResponse reindexKnowledgeDocument(@PathVariable String code) {
        return knowledgeDocumentService.reindex(code);
    }

    @Operation(summary = "Bật/tắt tài liệu tri thức", description = "Tắt = không còn gửi kèm RAG, vẫn giữ lịch sử")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PatchMapping("/knowledge-documents/{code}")
    public KnowledgeDocumentResponse toggleKnowledgeDocument(@PathVariable String code,
                                                              @RequestBody Map<String, Boolean> body,
                                                              @CurrentUser AuthUser actor) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return knowledgeDocumentService.setActive(code, active, actor);
    }

    @Operation(summary = "Xoá tài liệu tri thức")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @DeleteMapping("/knowledge-documents/{code}")
    public void deleteKnowledgeDocument(@PathVariable String code) {
        knowledgeDocumentService.delete(code);
    }
}
