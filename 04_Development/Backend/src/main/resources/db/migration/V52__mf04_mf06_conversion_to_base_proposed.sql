-- ═══════════════════════════════════════════════════════════════════════════════
-- V52__mf04_mf06_conversion_to_base_proposed.sql — quy đổi đơn vị mua hàng của NHÀ
-- CUNG CẤP về đơn vị chuẩn (baseUnit) của nguyên liệu (PROPOSED — chưa duyệt chính thức).
--
-- Nguồn: plan thiết kế "Đơn vị đo lường" (bản sửa sau góp ý: quy đổi bó↔gam / thùng↔hộp
-- KHÔNG PHẢI thuộc tính của nguyên liệu — mỗi NCC đóng gói khác nhau, nên field này
-- sống ở `supplier_products`/`partnership_catalog_items` (phía NCC/hợp đồng), KHÔNG ở
-- `catalog_ingredients`.
--
-- `conversion_to_base` — bao nhiêu ĐƠN VỊ CHUẨN (`catalog_ingredients.unit`) = 1 đơn vị
-- NCC đang bán (cột `unit`/`requested_unit` đã có). Mặc định 1 = trùng đơn vị chuẩn
-- (case phổ biến nhất, NCC không cần khai gì thêm).
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE supplier_products
  ADD COLUMN conversion_to_base NUMERIC(14, 6) NOT NULL DEFAULT 1,
  ADD COLUMN conversion_note VARCHAR(255);
ALTER TABLE supplier_products ALTER COLUMN conversion_to_base DROP DEFAULT;

ALTER TABLE partnership_catalog_items
  ADD COLUMN conversion_to_base NUMERIC(14, 6) NOT NULL DEFAULT 1,
  ADD COLUMN conversion_note VARCHAR(255);
ALTER TABLE partnership_catalog_items ALTER COLUMN conversion_to_base DROP DEFAULT;

ALTER TABLE catalog_change_requests
  ADD COLUMN requested_conversion_to_base NUMERIC(14, 6) NOT NULL DEFAULT 1,
  ADD COLUMN requested_conversion_note VARCHAR(255);
ALTER TABLE catalog_change_requests ALTER COLUMN requested_conversion_to_base DROP DEFAULT;
