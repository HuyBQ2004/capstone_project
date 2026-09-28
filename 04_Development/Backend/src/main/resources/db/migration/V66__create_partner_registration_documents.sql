-- V66: Tạo bảng partner_registration_documents — tài liệu đối tác (NCC/Đơn vị suất ăn) nộp
-- kèm lúc đăng ký Guest (UC-071/067). Bảng RIÊNG, nhẹ, tách khỏi partner_documents (ADR-M04-4)
-- vì form đăng ký Guest không thu number/issuedBy/issuedDate mà partner_documents yêu cầu
-- (11/08/2026) — mirror school_documents (V40).
CREATE TABLE IF NOT EXISTS partner_registration_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(32) NOT NULL UNIQUE,
    partner_id UUID NOT NULL REFERENCES partners(id) ON DELETE CASCADE,
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

CREATE INDEX IF NOT EXISTS ix_partner_reg_doc_partner ON partner_registration_documents(partner_id);
