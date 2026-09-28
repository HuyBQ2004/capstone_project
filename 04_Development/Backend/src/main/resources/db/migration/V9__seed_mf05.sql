-- V9__seed_mf05.sql — Seed dữ liệu MF05 (SYNTHETIC, dùng chung production/dev/test).
-- Nguồn: TDS-MF05_Backend.md v1.2 §5.2/§11.2 (5+ diet_groups, dish library, menu_templates)
-- + mock-data.ts DIET_GROUPS/DISH_LIBRARY/MENU_TEMPLATES — ngày dd/MM/yyyy và nhãn tiếng Việt
-- đã chuyển sang ISO/SCREAMING_SNAKE MỘT LẦN tại đây (D4/D6, M05-D05 — danh mục KHÔNG enum).
--
-- V1..V7 KHÔNG seed dữ liệu nghiệp vụ (chỉ V5__seed_mf03.sql/V7__seed_mf04.sql tự tạo trường
-- riêng của mình — cùng khuôn ở đây): `menu_templates` cần `school_id NOT NULL` nên MF05 tự
-- tạo MỘT trường riêng (KHÔNG đụng dải UUID/code của V900 test fixtures hay V5 MF03), để V9
-- chạy được trên DB trống (V900 chạy SAU V9 theo thứ tự Flyway version).

-- ───────────────────────── diet_groups (danh mục toàn hệ thống — M05-D05) ─────────────────────────
INSERT INTO diet_groups (id, code, label, match_diet, exclude_allergens, exclude_ingredient_keywords, note) VALUES
 ('55555555-5555-5555-5555-555555555001', 'VEGETARIAN',  'Ăn chay',              'Vegetarian',
  '[]', '["gà","cá","thịt","tôm","cua","nước mắm"]', 'Không dùng thực phẩm có nguồn gốc động vật'),
 ('55555555-5555-5555-5555-555555555002', 'DIABETIC',    'Tiểu đường',           'Diabetic',
  '[]', '["đường"]', 'Hạn chế đường/tinh bột nhanh'),
 ('55555555-5555-5555-5555-555555555003', 'HALAL',       'Halal',                'Halal',
  '[]', '["thịt lợn","rượu"]', 'Tuân thủ quy tắc Halal'),
 ('55555555-5555-5555-5555-555555555004', 'GLUTEN_FREE', 'Không gluten',         'Gluten-Free',
  '[]', '["lúa mì","bột mì"]', 'Không chứa gluten'),
 ('55555555-5555-5555-5555-555555555005', 'NUT_FREE',    'Không hạt',           'Nut-Free',
  '["Đậu phộng","Hạt điều"]', '[]', 'Không chứa các loại hạt'),
 ('55555555-5555-5555-5555-555555555006', 'LACTOSE_FREE','Không lactose',        'Lactose-Free',
  '["Sữa"]', '[]', 'Không chứa sản phẩm từ sữa động vật');
