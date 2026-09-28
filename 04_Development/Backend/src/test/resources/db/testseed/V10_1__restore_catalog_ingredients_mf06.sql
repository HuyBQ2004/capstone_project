-- ═══════════════════════════════════════════════════════════════════════════════
-- V10_1__restore_catalog_ingredients_mf06.sql — KHÔI PHỤC FIXTURE, CHỈ MÔI TRƯỜNG TEST
--
-- ⚠️ Nằm ở src/test/resources ⇒ KHÔNG BAO GIỜ lên classpath của prod.
--
-- Vì sao tách ra khỏi V11_1__restore_mf06.sql (đứng SAU V11 theo thứ tự Flyway): V11 có
-- `ALTER TABLE dish_ingredients ADD CONSTRAINT fk_dish_ing_catalog FOREIGN KEY
-- (ingredient_code) REFERENCES catalog_ingredients(code)` là CÂU LỆNH ĐẦU TIÊN của file đó.
-- `V9_1__restore_mf05.sql` (chạy trước V10) đã insert `dish_ingredients` với
-- `ingredient_code IN ('ING-CA-BASA','ING-DAU-PHU','ING-GAO','ING-TOM')`, nhưng
-- `catalog_ingredients` (bảng do V10 tạo) vẫn RỖNG tại thời điểm V11 chạy — vì bản seed của
-- nó nằm ở `V11_1`, đứng SAU V11 trong thứ tự Flyway (11 < 11.1). Kết quả đo được: MỌI
-- `@SpringBootTest` kế thừa `AbstractIamIntegrationTest` fail ngay ở `@BeforeEach` với
-- `FlywayMigrateException: ... violates foreign key constraint "fk_dish_ing_catalog"` (không
-- chỉ MF05 — chặn toàn bộ `*IT.java` của dự án).
--
-- Fix: seed `catalog_ingredients` ở version SỚM HƠN V11 (đặt giữa V10 — tạo bảng — và V11 —
-- thêm FK). Nội dung bản INSERT dưới đây nguyên văn từ `V11_1__restore_mf06.sql` (đã xoá khỏi
-- đó để tránh 2 nguồn sự thật) — `ON CONFLICT (code) DO NOTHING` nên chạy 1 hay nhiều lần đều
-- an toàn.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO catalog_ingredients (id, code, name, unit, storage_zone, refuse_factor) VALUES
 ('66666666-6666-6666-6666-666666666601', 'ING-CA-BASA',       'Cá basa phi lê',        'kg', 'CHILLED', 0.000),
 ('66666666-6666-6666-6666-666666666602', 'ING-DAU-PHU',       'Đậu phụ',               'kg', 'CHILLED', 0.000),
 ('66666666-6666-6666-6666-666666666603', 'ING-GAO',           'Gạo tẻ',                'kg', 'DRY',     0.000),
 ('66666666-6666-6666-6666-666666666604', 'ING-TOM',           'Tôm',                   'kg', 'FROZEN',  0.400),
 ('66666666-6666-6666-6666-666666666605', 'ING-THIT-LON-VAI',  'Thịt lợn nạc vai',      'kg', 'CHILLED', 0.050),
 ('66666666-6666-6666-6666-666666666606', 'ING-THIT-GA',       'Thịt gà ta',            'kg', 'CHILLED', 0.350),
 ('66666666-6666-6666-6666-666666666607', 'ING-TOM-SU',        'Tôm sú',                'kg', 'FROZEN',  0.450),
 ('66666666-6666-6666-6666-666666666608', 'ING-TRUNG-CUT',     'Trứng cút',             'quả','CHILLED', 0.180),
 ('66666666-6666-6666-6666-666666666609', 'ING-DAU-AN',        'Dầu ăn thực vật',       'lít','DRY',     0.000),
 ('66666666-6666-6666-6666-666666666610', 'ING-RAU-CAI-NGOT',  'Rau cải ngọt',          'kg', 'CHILLED', 0.150),
 ('66666666-6666-6666-6666-666666666611', 'ING-GAO-TAM',       'Gạo tấm',               'kg', 'DRY',     0.000)
ON CONFLICT (code) DO NOTHING;
