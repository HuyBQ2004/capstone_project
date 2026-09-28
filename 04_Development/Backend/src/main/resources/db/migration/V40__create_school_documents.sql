-- V40: Tạo bảng school_documents cho lưu trữ file/tài liệu đính kèm của Trường học (UC-070/UC-122)
CREATE TABLE IF NOT EXISTS school_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    school_id UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name VARCHAR(180) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_key VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(180),
    updated_by VARCHAR(180)
);

CREATE INDEX IF NOT EXISTS ix_school_doc_school ON school_documents(school_id);
