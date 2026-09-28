-- Bảng lưu file bằng chứng dùng CHUNG cho mf11 (thay vì mỗi module 1 bảng riêng như
-- violation_files/incident_evidence cũ) — cùng pattern owner_type/owner_id đã có ở
-- sample_attachments (mf08). Dùng cho: SOURCE_REPORT (ảnh kèm báo cáo của Teacher/Parent,
-- UC-073/074) và CORRECTIVE_ACTION (ảnh bằng chứng khắc phục, UC-082).
CREATE TABLE mf11_attachments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type      VARCHAR(24)  NOT NULL CHECK (owner_type IN ('SOURCE_REPORT','CORRECTIVE_ACTION')),
    owner_id        UUID         NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    storage_key     VARCHAR(255) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    checksum_sha256 VARCHAR(64)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),             -- D9
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(180),
    updated_by      VARCHAR(180)
);
CREATE INDEX ix_mf11_att_owner ON mf11_attachments(owner_type, owner_id);
