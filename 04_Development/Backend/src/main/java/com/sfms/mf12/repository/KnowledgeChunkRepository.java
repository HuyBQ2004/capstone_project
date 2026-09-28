package com.sfms.mf12.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code ai_knowledge_chunks} — đoạn tài liệu + vector nhúng (pgvector, V59).
 *
 * <p><b>Vì sao dùng native query chứ không JPA entity.</b> Kiểu {@code vector} của pgvector
 * không có sẵn trong Hibernate; muốn map thành entity phải thêm phụ thuộc
 * {@code pgvector-java} và một {@code UserType} riêng. Toàn bộ nhu cầu ở đây gói gọn trong ba
 * câu SQL (xoá theo tài liệu, chèn hàng loạt, tìm hàng xóm gần nhất), nên viết thẳng SQL rẻ hơn
 * và không giấu chi phí truy vấn sau một lớp trừu tượng.
 *
 * <p>Vector truyền dưới dạng chuỗi {@code "[0.1,0.2,…]"} rồi {@code CAST(? AS vector)} — đúng
 * khuôn text của pgvector, không cần driver hiểu kiểu này.
 */
@Repository
public class KnowledgeChunkRepository {

    @PersistenceContext
    private EntityManager em;

    /** Một đoạn kèm khoảng cách cosine tới câu hỏi (0 = trùng khớp hoàn toàn). */
    public record ScoredChunk(String content, double distance) {}

    @Transactional
    public void deleteByDocumentId(UUID documentId) {
        em.createNativeQuery("delete from ai_knowledge_chunks where document_id = :doc")
                .setParameter("doc", documentId)
                .executeUpdate();
    }

    /**
     * Chèn các đoạn của MỘT tài liệu. {@code embeddings} có thể rỗng — khi mô hình embedding
     * đang tắt, tài liệu vẫn được lưu với {@code embedding = NULL} và vẫn dùng được qua truy hồi
     * từ khoá; chặn việc nạp chỉ vì embedding hỏng là đánh đổi sai.
     */
    @Transactional
    public void insertChunks(UUID documentId, List<String> contents, List<List<Double>> embeddings) {
        boolean hasEmbeddings = embeddings != null && embeddings.size() == contents.size();
        for (int i = 0; i < contents.size(); i++) {
            var query = em.createNativeQuery(hasEmbeddings
                    ? "insert into ai_knowledge_chunks (document_id, ord, content, embedding) "
                      + "values (:doc, :ord, :content, CAST(:emb AS vector))"
                    : "insert into ai_knowledge_chunks (document_id, ord, content) "
                      + "values (:doc, :ord, :content)")
                    .setParameter("doc", documentId)
                    .setParameter("ord", i)
                    .setParameter("content", contents.get(i));
            if (hasEmbeddings) {
                query.setParameter("emb", toVectorLiteral(embeddings.get(i)));
            }
            query.executeUpdate();
        }
    }

    /**
     * Tìm {@code limit} đoạn gần nhất với vector câu hỏi, CHỈ trong tài liệu đang bật.
     *
     * <p>{@code <=>} là khoảng cách cosine — phải khớp với {@code vector_cosine_ops} của index
     * HNSW ở V59; dùng lệch toán tử thì index bị bỏ qua âm thầm, kết quả vẫn đúng nhưng quét
     * toàn bảng.
     */
    @Transactional(readOnly = true)
    public List<ScoredChunk> searchNearest(List<Double> queryEmbedding, int limit) {
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            return List.of();
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
                select c.content, c.embedding <=> CAST(:emb AS vector) as distance
                from ai_knowledge_chunks c
                join ai_knowledge_documents d on d.id = c.document_id
                where c.embedding is not null and d.active = true
                order by distance
                limit :lim
                """)
                .setParameter("emb", toVectorLiteral(queryEmbedding))
                .setParameter("lim", limit)
                .getResultList();

        List<ScoredChunk> out = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            out.add(new ScoredChunk((String) row[0], ((Number) row[1]).doubleValue()));
        }
        return out;
    }

    /** Số đoạn ĐÃ có embedding trong các tài liệu đang bật — dùng để quyết định có tìm vector không. */
    @Transactional(readOnly = true)
    public long countEmbedded() {
        Object n = em.createNativeQuery("""
                select count(*) from ai_knowledge_chunks c
                join ai_knowledge_documents d on d.id = c.document_id
                where c.embedding is not null and d.active = true
                """).getSingleResult();
        return ((Number) n).longValue();
    }

    private static String toVectorLiteral(List<Double> vector) {
        StringBuilder sb = new StringBuilder(vector.size() * 8).append('[');
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector.get(i));
        }
        return sb.append(']').toString();
    }
}
