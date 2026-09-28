

-- ⚠️ Dời từ V10 sang đây (phát hiện ở Bước 2, không có trong TDS v1.1 — xem ghi chú ở V10):
-- catalog_ingredients chỉ CÓ DỮ LIỆU từ đây trở đi; ADD CONSTRAINT lúc V10 (khi bảng còn rỗng)
-- sẽ fail ngay vì V9 (đã merge) đã ghi sẵn ingredient_code IN ('ING-CA-BASA','ING-DAU-PHU',
-- 'ING-GAO','ING-TOM') vào dish_ingredients. FK trỏ `code` (không trỏ `id`) đúng như TDS §5.2/
-- M06-D04 đã quyết — chỉ đổi VỊ TRÍ câu lệnh, không đổi nội dung.
ALTER TABLE dish_ingredients
  ADD CONSTRAINT fk_dish_ing_catalog
  FOREIGN KEY (ingredient_code) REFERENCES catalog_ingredients(code);
UPDATE user_accounts SET partner_id = '99999999-9999-9999-9999-999999990001' WHERE code = 'U-9907';

-- ───────────────────────── code_sequences — tránh trùng mã khi POST không gửi id (D5) ─────────────────────────
INSERT INTO code_sequences (scope, next_value) VALUES
 ('mf06_purchase_requests:2026', 100),
 ('mf06_deliveries:2026', 100),
 ('mf06_receiving:20260702', 100),
 ('mf06_txn:20260728', 100),
 ('mf06_prep:20260707', 100),
 ('mf06_issue_slip:2026', 100)
ON CONFLICT (scope) DO UPDATE SET next_value = EXCLUDED.next_value;
