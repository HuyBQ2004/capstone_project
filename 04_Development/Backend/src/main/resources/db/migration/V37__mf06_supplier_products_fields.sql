-- V37__mf06_supplier_products_fields.sql — bổ sung field FE đang cần cho `supplier_products`
-- (M06-D03 tạo bảng ở V10 nhưng chỉ có code/partner_id/ingredient_id/unit_price/active —
-- chưa từng có controller nào expose resource này, FE đọc mảng tĩnh SUPPLIER_PRODUCTS mock).
--
-- `label` — tên NCC dùng trên hoá đơn/bao bì, có thể khác tên danh mục chuẩn.
-- `unit`  — đơn vị báo giá của DÒNG GIÁ này (NCC có thể báo theo đơn vị khác `catalog_ingredients.unit`,
--           VD danh mục ghi 'kg' nhưng NCC báo theo 'thùng 10kg' quy đổi riêng) — NOT NULL, FE luôn gửi.
-- `packaging`     — quy cách đóng gói, dùng làm tròn số lượng đặt hàng.
-- `effective_from` — ngày giá này có hiệu lực (NCC đổi giá thì tạo dòng mới, không sửa giá cũ).
ALTER TABLE supplier_products
  ADD COLUMN label VARCHAR(120),
  ADD COLUMN unit VARCHAR(20) NOT NULL DEFAULT 'kg',
  ADD COLUMN packaging VARCHAR(120),
  ADD COLUMN effective_from DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE supplier_products ALTER COLUMN unit DROP DEFAULT;
ALTER TABLE supplier_products ALTER COLUMN effective_from DROP DEFAULT;
