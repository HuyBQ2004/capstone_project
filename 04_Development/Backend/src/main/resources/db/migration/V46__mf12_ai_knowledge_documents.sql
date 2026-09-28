-- Kho tri thức cho AI (RAG) — nguồn bền vững thay cho KNOWLEDGE_BASE in-memory của
-- rag_service (mất khi container restart). System Admin quản trị qua
-- `POST/GET/DELETE /api/v1/ai/knowledge-documents`; nội dung active được Java gửi kèm
-- mỗi lượt gọi RAG (`context_docs`) thay vì để RAG tự khớp từ khoá trong KB tĩnh của nó.
-- Dữ liệu cấp nền tảng (không gắn school_id) — dùng chung cho toàn hệ thống.
CREATE TABLE ai_knowledge_documents (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(32)  NOT NULL,                                -- D1 — "AIK-2026-0001"
    title       VARCHAR(200) NOT NULL,
    category    VARCHAR(32)  NOT NULL
        CHECK (category IN ('ATTP_REGULATION','NUTRITION_STANDARD','SCHOOL_POLICY','OTHER')),
    content     TEXT         NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),                  -- D9
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(180),
    updated_by  VARCHAR(180)
);
CREATE UNIQUE INDEX uq_ai_kb_code ON ai_knowledge_documents (code);
CREATE INDEX ix_ai_kb_active ON ai_knowledge_documents (active, created_at DESC);
