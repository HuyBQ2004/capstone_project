-- ═══════════════════════════════════════════════════════════════════════════════
-- V53__mf05_dish_ingredient_quantity_numeric_proposed.sql — đổi `dish_ingredients.
-- quantity` từ chuỗi tự do sang số (PROPOSED — chưa duyệt chính thức).
--
-- Nguồn: plan thiết kế "Đơn vị đo lường" §Phase B.8. Cột này trước giờ là VARCHAR,
-- không validate được là số, có thể lưu rác — không có nơi nào trong hệ thống thực
-- sự dùng giá trị phi số (mọi UI nhập bằng `<input type="number">`), xác nhận qua
-- dữ liệu thật trên môi trường dev (chỉ toàn số nguyên/thập phân, đơn vị luôn "g").
-- USING quantity::numeric sẽ FAIL nếu còn dòng nào không phải số — đúng ý muốn,
-- để lộ ra ngay lúc migrate thay vì âm thầm sai lệch về sau.
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE dish_ingredients
  ALTER COLUMN quantity TYPE NUMERIC(12, 3) USING quantity::numeric,
  ALTER COLUMN quantity DROP NOT NULL;
