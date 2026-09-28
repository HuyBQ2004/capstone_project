-- ============================================================================
-- V74 (13/08/2026, theo Nam) — ALLERGEN Ở CẤP NGUYÊN LIỆU.
--
-- VÌ SAO
-- ------
-- BR-063 nói tác nhân dị ứng của món được DẪN XUẤT từ thành phần. Nhưng backend
-- chưa từng có dữ liệu đó: `catalog_ingredients` chỉ có code/name/unit/storage_zone/
-- refuse_factor/active. Toàn bộ tri thức "tôm → Tôm, bột mì → Gluten" nằm trong một
-- bảng TỪ KHOÁ HARDCODE Ở FRONTEND (`ALLERGEN_KEYWORDS`, lib/domain-utils.ts).
--
-- Hệ quả đo được trước migration này:
--   1. `AllergyConflictChecker.check()` chỉ đọc `dishes.manual_allergens` — mà cột đó
--      theo đúng định nghĩa chỉ là phần KHAI THÊM BẰNG TAY cho nguyên liệu ngoài danh
--      mục. Món khai thành phần tử tế thì mảng đó RỖNG ⇒ server báo 0 xung đột trong
--      khi frontend (tự suy từ tên nguyên liệu) báo có. Server luôn là bên "nhẹ" hơn.
--   2. Cùng gốc, `ReferenceResolver.standardMenuSatisfiesDietGroup` tuyên bố một thực
--      đơn có thịt lợn là "HALAL ăn được" nếu tên món không chứa từ khoá.
--   3. Vì server không tính nổi, mọi màn cần BR-063 phải TẢI TOÀN BỘ sổ học sinh +
--      khai báo dị ứng về trình duyệt để tự tính (24 call-site `useAllPages`).
--
-- CÁI GÌ ĐÚNG Ở ĐÂY
-- -----------------
-- Allergen là thuộc tính của NGUYÊN LIỆU, không phải của chuỗi ký tự trong tên nó.
-- Cột này biến nó thành dữ liệu khai báo được, sửa được, kiểm toán được — thay cho
-- việc đoán theo tên ở hai nơi bằng hai ngôn ngữ.
--
-- BACKFILL DƯỚI ĐÂY LÀ SUY ĐOÁN, KHÔNG PHẢI SỰ THẬT
-- --------------------------------------------------
-- Nó chép đúng bảng từ khoá của frontend để hành vi KHÔNG ĐỔI ngay sau khi deploy
-- (không bỗng dưng mất cảnh báo đang có). Nhưng nó vẫn là so khớp chuỗi và CHẮC CHẮN
-- sai ở một số dòng — VD "Dầu hào" có hải sản mà không khớp từ khoá nào; "Cà chua"
-- không được khớp "cá " (đó là lý do từ khoá có dấu cách ở cuối).
-- => Việc còn lại của NGƯỜI: rà 198 dòng ở màn System Admin › Danh mục nguyên liệu.
--    Sau khi rà xong, frontend sẽ bỏ hẳn bảng từ khoá và chỉ đọc cột này.
-- ============================================================================

ALTER TABLE catalog_ingredients
  ADD COLUMN allergens JSONB NOT NULL DEFAULT '[]'::jsonb;

COMMENT ON COLUMN catalog_ingredients.allergens IS
  'BR-063 — tác nhân dị ứng của nguyên liệu này, mảng chuỗi khớp danh mục ALLERGENS. '
  'Nguồn sự thật để dẫn xuất allergen của MÓN (union với dishes.manual_allergens). '
  'Seed ban đầu suy từ tên (V74) — cần người rà lại.';

-- ── Backfill: chép ALLERGEN_KEYWORDS của frontend, cùng cách khớp (substring trên
--    chuỗi đã hạ thường và đệm 2 dấu cách — `lib/domain-utils.ts:detectAllergens`). ──
WITH keywords(allergen, kw) AS (
  VALUES
    ('Tôm',              ARRAY['tôm']),
    ('Cua',              ARRAY['cua', 'ghẹ']),
    -- 'cá ' có dấu cách cuối: chặn khớp nhầm "Cà chua", "Cà rốt", "Cải bó xôi".
    ('Cá',               ARRAY['cá ']),
    ('Hải sản',          ARRAY['mực', 'ngao', 'sò', 'ốc', 'hàu', 'hải sản']),
    ('Trứng',            ARRAY['trứng']),
    ('Sữa bò',           ARRAY['sữa bò', 'sữa tươi', 'phô mai', 'bơ động vật', 'sữa chua', 'kem sữa']),
    ('Đậu phộng',        ARRAY['đậu phộng', 'lạc', 'dầu lạc']),
    ('Đậu nành',         ARRAY['đậu nành', 'đậu phụ', 'đậu hũ', 'nước tương', 'tào phớ', 'sữa đậu nành']),
    ('Lúa mì (Gluten)',  ARRAY['bột mì', 'bánh mì', 'mì sợi', 'mì ống', 'nui', 'lúa mì']),
    ('Mè (vừng)',        ARRAY['mè', 'vừng', 'dầu mè'])
),
matched AS (
  SELECT ci.id, jsonb_agg(DISTINCT k.allergen) AS allergens
  FROM catalog_ingredients ci
  JOIN keywords k
    ON EXISTS (
      SELECT 1 FROM unnest(k.kw) AS one_kw
      WHERE ' ' || lower(ci.name) || ' ' LIKE '%' || one_kw || '%'
    )
  GROUP BY ci.id
)
UPDATE catalog_ingredients ci
SET allergens = m.allergens
FROM matched m
WHERE ci.id = m.id;
