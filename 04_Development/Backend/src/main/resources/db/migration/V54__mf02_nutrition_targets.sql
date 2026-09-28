-- ═══════════════════════════════════════════════════════════════════════════════
-- V54__mf02_nutrition_targets.sql — chuẩn dinh dưỡng bữa trưa THEO TRƯỜNG × KHỐI.
--
-- Trước migration này chuẩn BR-062 là MỘT hằng số duy nhất cho toàn hệ thống, khai
-- TRÙNG ở hai nơi và không sửa được nếu không build lại:
--   - com.sfms.mf09.service.MealHistoryServiceImpl.NUTRITION_TARGET (private static final)
--   - Frontend  src/lib/mock-data.ts  NUTRITION_TARGET
-- cùng giá trị 715 kcal / 28g đạm / 22g béo / 95g carb (nhóm tuổi 6–11, tiểu học).
--
-- Vì sao phải tách theo KHỐI: `menus` gắn `grade_id` (V44) và màn duyệt thực đơn của
-- Ban giám hiệu đánh dấu suất lệch ±10% so với chuẩn — dùng chung một con số nghĩa là
-- thực đơn Khối 1 và Khối 5 bị chấm cùng một thước. Với trường ngoài tiểu học (mầm non/
-- THCS) thì cảnh báo sai theo hướng NGUY HIỂM: bữa ăn mầm non đủ chuẩn bị đánh dấu là
-- thiếu năng lượng.
--
-- Vì sao theo TRƯỜNG: cùng lý do `school_config.cost_ceiling` (BR-080) là per-school —
-- chuẩn dinh dưỡng là quyết định của Ban giám hiệu/y tế từng trường, không phải hằng số
-- vận hành toàn hệ thống như `diet_groups`.
--
-- KHÔNG backfill: trường chưa khai thì service trả hằng mặc định 715/28/22/95 (xem
-- NutritionTargetServiceImpl.DEFAULT_TARGET) — giữ NGUYÊN hành vi cũ cho mọi trường hiện
-- có, không ép migrate, và không sinh ra hàng nghìn dòng "cấu hình" mà chẳng ai đặt.
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE nutrition_targets (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id),
    grade_id    UUID NOT NULL REFERENCES grades(id) ON DELETE CASCADE,
    kcal        NUMERIC(8,2) NOT NULL,
    protein     NUMERIC(8,2) NOT NULL,
    fat         NUMERIC(8,2) NOT NULL,
    carb        NUMERIC(8,2) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),   -- D9 BaseEntity
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(180),
    updated_by  VARCHAR(180),
    -- Một khối chỉ có ĐÚNG một chuẩn. `grade_id` đã unique một mình là đủ (grades.school_id
    -- NOT NULL nên khối không thể thuộc 2 trường), nhưng ràng buộc theo cặp mới nói đúng ý
    -- định và cho index phục vụ luôn truy vấn "chuẩn của trường tôi".
    CONSTRAINT uq_nutrition_target_school_grade UNIQUE (school_id, grade_id),
    -- Chuẩn dinh dưỡng phải là số DƯƠNG: 0 kcal không phải "chưa khai" mà là dữ liệu sai —
    -- "chưa khai" biểu diễn bằng KHÔNG CÓ DÒNG, không phải bằng số 0.
    CONSTRAINT ck_nutrition_target_positive CHECK (
        kcal > 0 AND protein > 0 AND fat > 0 AND carb > 0
    )
);
CREATE INDEX ix_nutrition_targets_school ON nutrition_targets(school_id);
