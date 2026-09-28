-- =========================================================
-- MF05 — 3 việc chốt cùng đợt (07_implement/05_Menu_Grade_Diet_Coverage_Dish_Media_Design.md):
--   (1) menu_templates.grade — NULLABLE, khối trống = dùng chung mọi khối (đúng quyết định
--       Nam, không bắt buộc mọi mẫu phải gắn khối).
--   (2) mở rộng ràng buộc "không trùng menu" sang cả audience đặc biệt (chế độ ăn riêng) —
--       trước đây ux_menu_all_per_slot (V8) chỉ chặn trùng audience=ALL.
--   (3) dishes — thêm ảnh/mô tả/hướng dẫn chế biến (chưa từng có, theo yêu cầu người dùng).
-- =========================================================

ALTER TABLE menu_templates ADD COLUMN grade VARCHAR(16);

ALTER TABLE dishes
    ADD COLUMN image_url            VARCHAR(500),
    ADD COLUMN description          VARCHAR(2000),
    ADD COLUMN cooking_instructions TEXT;

-- Mỗi (khối, ngày, buổi, nhóm chế độ ăn) chỉ 1 menu — cùng tinh thần ux_menu_all_per_slot (V8)
-- nhưng cho audience KHÁC ALL (diet_group_id NOT NULL). Hai index KHÔNG chồng lấn nhau vì loại
-- trừ lẫn nhau theo diet_group_id IS NULL / IS NOT NULL.
CREATE UNIQUE INDEX ux_menu_diet_per_slot
    ON menus (school_id, menu_date, grade, meal_type, diet_group_id)
    WHERE diet_group_id IS NOT NULL AND status <> 'REJECTED';
