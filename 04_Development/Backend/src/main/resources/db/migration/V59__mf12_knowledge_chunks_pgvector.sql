-- MF12 — TRUY HỒI BẰNG VECTOR cho kho tri thức AI (09/08/2026, theo yêu cầu của Nam).
--
-- Vì sao có: trước bản này, "RAG service" chỉ có chữ RAG trong tên — không embedding, không
-- vector store; backend gửi nguyên văn tài liệu sang rồi nhét vào prompt. Lớp chọn lọc tạm thời
-- (KnowledgeContextSelector) truy hồi bằng TỪ KHOÁ + trọng số độ hiếm; đủ dùng cho 7 văn bản
-- nhưng đuối ngay khi câu hỏi diễn đạt khác chữ trong luật ("báo cho ai" vs "thông báo cơ quan
-- có thẩm quyền").
--
-- Điều kiện tiên quyết: image Postgres đổi từ `postgres:15-alpine` sang `pgvector/pgvector:pg15`
-- (xem docker-compose.yml). Alpine KHÔNG có extension này.
CREATE EXTENSION IF NOT EXISTS vector;

-- 768 chiều — CỐ Ý khớp cả hai nhà cung cấp đang dùng, để đổi provider không phải đổi schema:
--   · Gemini  `text-embedding-004`                 → 768
--   · LM Studio `text-embedding-nomic-embed-text-v1.5` → 768 (đo thật 09/08/2026)
-- Đổi sang mô hình khác chiều ⇒ phải migration lại cột + sinh lại toàn bộ embedding.
CREATE TABLE ai_knowledge_chunks (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id  UUID         NOT NULL REFERENCES ai_knowledge_documents(id) ON DELETE CASCADE,
    ord          INT          NOT NULL,                       -- thứ tự đoạn trong tài liệu gốc
    content      TEXT         NOT NULL,
    embedding    vector(768),                                 -- NULL = chưa sinh được (xem dưới)
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),         -- D9
    UNIQUE (document_id, ord)
);

COMMENT ON COLUMN ai_knowledge_chunks.embedding IS
    'NULL khi nhà cung cấp embedding không gọi được lúc nạp tài liệu. Tài liệu vẫn được lưu và '
    'vẫn dùng được qua truy hồi từ khoá — KHÔNG chặn việc nạp chỉ vì mô hình embedding đang tắt. '
    'Sinh lại bằng POST /ai/knowledge-documents/{code}/reindex.';

-- ON DELETE CASCADE ở trên đã lo việc xoá tài liệu; index này cho truy vấn "đoạn của tài liệu X".
CREATE INDEX idx_knowledge_chunks_document ON ai_knowledge_chunks (document_id);

-- HNSW thay vì IVFFlat: IVFFlat cần dữ liệu có sẵn để huấn luyện danh sách (bảng rỗng lúc tạo
-- index ⇒ chất lượng kém), HNSW dựng dần theo từng lần chèn nên hợp với kho tri thức lớn dần.
-- `vector_cosine_ops` khớp toán tử `<=>` dùng ở KnowledgeChunkRepository — dùng lệch toán tử là
-- index bị bỏ qua âm thầm, truy vấn vẫn ĐÚNG nhưng quét toàn bảng.
CREATE INDEX idx_knowledge_chunks_embedding
    ON ai_knowledge_chunks USING hnsw (embedding vector_cosine_ops);
