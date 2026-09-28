-- =========================================================
-- V47__seed_mf07_three_step_inspection.sql
-- Seed dữ liệu mẫu đầy đủ thực tế cho:
--   1. Thực đơn các ngày (Menu hôm nay 06/08, 07/08, 05/08) — Điều kiện BẮT BUỘC để mở kiểm thực
--   2. Kho tồn kho nguyên liệu (Inventory Batches dồi dào, sẵn sàng xuất kho)
--   3. Luồng Kiểm thực 3 bước (MF07 — QĐ 1246/QĐ-BYT) đủ 3 Bước + Đánh giá y tế + BGH ký duyệt
-- =========================================================

-- ───────────────────────── 1. THỰC ĐƠN HÔM NAY & CÁC NGÀY (menus & menu_dishes) ─────────────────────────
-- Đảm bảo có sẵn grades cho các trường (Khối 1..12)
INSERT INTO grades (school_id, name, sort_order)
SELECT s.id, 'Khối ' || g.n, g.n
FROM schools s
CROSS JOIN generate_series(1, 12) AS g(n)
ON CONFLICT (school_id, name) DO NOTHING;
