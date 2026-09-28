-- ═══════════════════════════════════════════════════════════════════════════════
-- V51__common_measure_units_proposed.sql — bảng tra cứu đơn vị đo lường hợp lệ,
-- dùng chung toàn hệ thống (PROPOSED — chưa duyệt chính thức).
--
-- Nguồn: kế hoạch thiết kế "Đơn vị đo lường" (07-08/2026), lưu ở
-- C:\Users\namng\.claude\plans\snazzy-churning-sonnet.md tại thời điểm viết migration
-- này — sau này nên chép nội dung quyết định vào 07_implement/ nếu được duyệt chính thức.
--
-- Bảng DỮ LIỆU (không phải Java enum) vì đơn vị đếm (bó/mớ/nải/thùng...) sẽ liên tục
-- phát sinh theo nguyên liệu/nhà cung cấp mới — ép vào enum sẽ phải deploy code mỗi
-- lần thêm. `factor_to_base` CHỈ có giá trị cho WEIGHT/VOLUME (quy đổi CHÍNH XÁC,
-- vật lý bất biến, không phụ thuộc nguyên liệu/NCC nào — VD g→kg luôn ×0.001).
-- COUNT không có factor nào ở đây: quy đổi đếm↔khối lượng (VD "1 bó ≈ 300g") KHÔNG
-- PHẢI hằng số chung — nó tùy theo cách từng NHÀ CUNG CẤP đóng gói, nên sống ở lớp
-- supplier/hợp đồng (`supplier_products.conversion_to_base`), KHÔNG ở bảng này.
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE measure_units (
  code VARCHAR(16) PRIMARY KEY,
  family VARCHAR(10) NOT NULL CHECK (family IN ('WEIGHT', 'VOLUME', 'COUNT')),
  -- Quy đổi CHÍNH XÁC về đơn vị gốc của family (kg cho WEIGHT, lít cho VOLUME) —
  -- đơn vị gốc tự quy đổi = 1. NULL với COUNT (không có "gốc" chung cho đơn vị đếm).
  factor_to_base NUMERIC(12, 6)
);

INSERT INTO measure_units (code, family, factor_to_base) VALUES
  ('kg', 'WEIGHT', 1),
  ('g', 'WEIGHT', 0.001),
  ('lít', 'VOLUME', 1),
  ('ml', 'VOLUME', 0.001),
  ('quả', 'COUNT', NULL),
  ('hộp', 'COUNT', NULL),
  ('bó', 'COUNT', NULL),
  ('mớ', 'COUNT', NULL),
  ('nải', 'COUNT', NULL),
  ('thùng', 'COUNT', NULL),
  ('bao', 'COUNT', NULL),
  ('ổ', 'COUNT', NULL),
  ('muỗng', 'COUNT', NULL);
