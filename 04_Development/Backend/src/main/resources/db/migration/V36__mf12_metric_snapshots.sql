-- V36__mf12_metric_snapshots.sql — UC-149 lịch sử CPU/heap/DB pool cho biểu đồ "monitoring".
-- ADR-M12-04 (ghi lúc merge SystemMetricsServiceImpl) chỉ tính điểm hiện tại từ JVM/DB pool,
-- không lưu lịch sử — bảng này bổ sung lịch sử tối thiểu (không dùng Prometheus/Grafana để
-- tránh thêm hạ tầng, cùng tinh thần ADR-000: Postgres đã đủ cho khối lượng ghi 15'/lần).
CREATE TABLE system_metric_snapshots (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  captured_at TIMESTAMPTZ NOT NULL,
  cpu_usage_percent DOUBLE PRECISION NOT NULL,
  memory_used_bytes BIGINT NOT NULL,
  memory_max_bytes BIGINT NOT NULL,
  active_db_connections INT NOT NULL,
  uptime_seconds BIGINT NOT NULL
);
CREATE INDEX ix_metric_snap_captured ON system_metric_snapshots(captured_at DESC);
