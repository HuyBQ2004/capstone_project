package com.sfms.mf12.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.mf12.domain.KnowledgeCategory;
import com.sfms.mf12.domain.KnowledgeDocument;
import com.sfms.mf12.repository.KnowledgeChunkRepository;
import com.sfms.mf12.repository.KnowledgeDocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Truy hồi LAI cho kho tri thức AI — {@code KnowledgeDocumentServiceImpl.contextFor}.
 *
 * <p><b>Vì sao bộ test này tồn tại.</b> Bản pgvector đầu tiên (09/08/2026) thay hẳn truy hồi từ
 * khoá bằng vector, và đo trên kho tri thức thật cho ra một <b>hồi quy</b>: câu "Kiểm thực ba
 * bước gồm những bước nào?" trả về nội dung kiểm tra hàng <b>NHẬP KHẨU</b>, trong khi bản từ khoá
 * trước đó trích đúng Điều 3 QĐ 1246. Ngược lại, câu diễn đạt đời thường ("cả lớp đau bụng sau
 * bữa trưa") thì chỉ vector hiểu.
 *
 * <p>Kết luận: không đường nào thắng tuyệt đối ⇒ dùng cả hai. Bộ test khoá lại ba tính chất mà
 * một lần "dọn dẹp" sau này rất dễ phá.
 */
class KnowledgeHybridRetrievalTest {

    private final KnowledgeDocumentRepository docs = mock(KnowledgeDocumentRepository.class);
    private final KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
    private final GeminiClient gemini = mock(GeminiClient.class);
    private final CodeGenerator codes = mock(CodeGenerator.class);
    private final KnowledgeTextExtractor extractor = mock(KnowledgeTextExtractor.class);

    private final KnowledgeDocumentServiceImpl service = new KnowledgeDocumentServiceImpl(
            docs, chunks, gemini, codes, extractor, new KnowledgeContextSelector(),
            Clock.fixed(Instant.parse("2026-08-09T03:00:00Z"), ZoneOffset.UTC));

    private static KnowledgeDocument doc(String content) {
        KnowledgeDocument d = new KnowledgeDocument();
        d.setCode("AIK-2026-0001");
        d.setTitle("t");
        d.setCategory(KnowledgeCategory.ATTP_REGULATION);
        d.setContent(content);
        d.setActive(true);
        return d;
    }

    @Test
    @DisplayName("Có embedding ⇒ lấy CẢ đoạn từ vector LẪN đoạn từ từ khoá")
    void usesBothRetrievers() {
        when(chunks.countEmbedded()).thenReturn(5L);
        when(gemini.embed(anyList(), eq("query"))).thenReturn(List.of(List.of(0.1, 0.2)));
        when(chunks.searchNearest(anyList(), anyInt()))
                .thenReturn(List.of(new KnowledgeChunkRepository.ScoredChunk("DOAN-TU-VECTOR", 0.1)));
        when(docs.findByActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(doc("Kiem thuc ba buoc gom ba giai doan kiem tra.")));

        List<String> context = service.contextFor("Kiểm thực ba bước gồm những bước nào?");

        assertThat(context).contains("DOAN-TU-VECTOR");
        assertThat(context).anyMatch(c -> c.contains("Kiem thuc ba buoc"));
    }

    @Test
    @DisplayName("Chưa có embedding nào ⇒ KHÔNG gọi mô hình embedding, đi thẳng từ khoá")
    void skipsVectorWhenNothingIndexed() {
        // Gọi embed khi bảng chunks rỗng là một lượt gọi mạng thừa trên MỌI câu hỏi.
        when(chunks.countEmbedded()).thenReturn(0L);
        when(docs.findByActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(doc("Mau thuc pham phai luu it nhat 24 gio.")));

        List<String> context = service.contextFor("luu mau bao nhieu gio");

        assertThat(context).isNotEmpty();
        verify(gemini, never()).embed(anyList(), any());
        verify(chunks, never()).searchNearest(anyList(), anyInt());
    }

    @Test
    @DisplayName("Mô hình embedding hỏng ⇒ vẫn trả ngữ cảnh bằng từ khoá, không ném lỗi")
    void survivesEmbeddingOutage() {
        when(chunks.countEmbedded()).thenReturn(5L);
        when(gemini.embed(anyList(), eq("query"))).thenReturn(List.of());   // hỏng
        when(docs.findByActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(doc("Mau thuc pham phai luu it nhat 24 gio.")));

        List<String> context = service.contextFor("luu mau bao nhieu gio");

        assertThat(context).isNotEmpty();
        verify(chunks, never()).searchNearest(anyList(), anyInt());
    }

    @Test
    @DisplayName("Đoạn trùng nhau giữa hai đường chỉ xuất hiện MỘT lần")
    void deduplicatesOverlap() {
        String shared = "Mau thuc pham phai luu it nhat 24 gio o nhiet do tu 2 den 8 do C.";
        when(chunks.countEmbedded()).thenReturn(5L);
        when(gemini.embed(anyList(), eq("query"))).thenReturn(List.of(List.of(0.1, 0.2)));
        when(chunks.searchNearest(anyList(), anyInt()))
                .thenReturn(List.of(new KnowledgeChunkRepository.ScoredChunk(shared, 0.05)));
        when(docs.findByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(doc(shared)));

        List<String> context = service.contextFor("luu mau bao nhieu gio");

        assertThat(context.stream().filter(shared::equals).count()).isEqualTo(1);
    }
}
