-- V35__mf12_backups.sql — BR-160/161: lịch sử sao lưu CSDL thật (trước đây SystemStatusServiceImpl
-- trả rỗng trung thực vì chưa có bảng/scheduler — nay bổ sung).
CREATE TABLE backups (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(32) NOT NULL,                                 -- D1: 'BKP-2026-0001', JSON trả dưới tên "id"
  started_at TIMESTAMPTZ NOT NULL,
  finished_at TIMESTAMPTZ,
  status VARCHAR(16) NOT NULL CHECK (status IN ('RUNNING', 'SUCCESS', 'FAILED')),
  size_bytes BIGINT NOT NULL DEFAULT 0,
  file_path VARCHAR(300),
  error_message VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),             -- D9
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180),
  CONSTRAINT uq_backups_code UNIQUE (code)
);
CREATE INDEX ix_backups_started ON backups(started_at DESC);
