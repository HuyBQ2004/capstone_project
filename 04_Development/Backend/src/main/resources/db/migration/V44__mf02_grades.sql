-- ═══════════════════════════════════════════════════════════════════════════════
-- V44__mf02_grades.sql — MF02 school-scoped `grades` table, thay cho:
--   - classrooms.grade   INT  CHECK BETWEEN 1 AND 12 (BR-024, global)
--   - menus.grade        VARCHAR(16)  free-text ("Khối 5"/"5")
--   - menu_templates.grade VARCHAR(16) free-text, NULLABLE
-- reconciled trước đây bằng regex extractGradeNumber() ở
-- com.sfms.common.reference.ReferenceResolver — migration này thêm FK UUID thật
-- (grade_id -> grades.id) ở cả 3 bảng và bỏ hoàn toàn việc so khớp bằng regex.
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE grades (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id   UUID NOT NULL REFERENCES schools(id),
    name        VARCHAR(32) NOT NULL,
    sort_order  INT NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    created_by  VARCHAR(180),
    updated_by  VARCHAR(180),
    CONSTRAINT uq_grade_school_name UNIQUE (school_id, name)
);
CREATE INDEX ix_grades_school ON grades(school_id);

-- ─── Backfill: 12 khối ("Khối 1".."Khối 12") cho MỌI trường đã từng dùng grade ─────────────
-- (classrooms.school_id UNION menus.school_id UNION menu_templates.school_id — cả 3 bảng có
-- thể có school_id không trùng nhau tuyệt đối, nên hợp cả 3 nguồn cho chắc).
INSERT INTO grades (school_id, name, sort_order)
SELECT s.id, 'Khối ' || g.n, g.n
FROM (
    SELECT school_id FROM classrooms
    UNION
    SELECT school_id FROM menus
    UNION
    SELECT school_id FROM menu_templates
) s(id)
CROSS JOIN generate_series(1, 12) AS g(n)
ON CONFLICT (school_id, name) DO NOTHING;

-- ─── classrooms.grade_id ───────────────────────────────────────────────────────
ALTER TABLE classrooms ADD COLUMN grade_id UUID REFERENCES grades(id);

UPDATE classrooms c
SET grade_id = g.id
FROM grades g
WHERE g.school_id = c.school_id AND g.name = 'Khối ' || c.grade;

DO $$
DECLARE missing INT;
BEGIN
    SELECT COUNT(*) INTO missing FROM classrooms WHERE grade_id IS NULL;
    IF missing > 0 THEN
        RAISE EXCEPTION 'V44: % classrooms rows failed to backfill grade_id', missing;
    END IF;
END $$;

ALTER TABLE classrooms ALTER COLUMN grade_id SET NOT NULL;

DROP INDEX IF EXISTS ix_class_ay_grade;
ALTER TABLE classrooms DROP CONSTRAINT IF EXISTS ck_class_grade;
ALTER TABLE classrooms DROP COLUMN grade;

CREATE INDEX ix_class_ay_grade ON classrooms(academic_year_id, grade_id);

-- ─── menus.grade_id — nguồn cũ là free-text ("Khối 5"/"5"), tách số trước khi join ─────────
ALTER TABLE menus ADD COLUMN grade_id UUID REFERENCES grades(id);

UPDATE menus m
SET grade_id = g.id
FROM grades g
WHERE g.school_id = m.school_id
  AND g.name = 'Khối ' || substring(m.grade from '\d+');

DO $$
DECLARE missing INT;
BEGIN
    SELECT COUNT(*) INTO missing FROM menus WHERE grade_id IS NULL;
    IF missing > 0 THEN
        RAISE EXCEPTION 'V44: % menus rows failed to backfill grade_id', missing;
    END IF;
END $$;

ALTER TABLE menus ALTER COLUMN grade_id SET NOT NULL;

DROP INDEX IF EXISTS ix_menus_school_date_grade;
DROP INDEX IF EXISTS ux_menu_all_per_slot;
DROP INDEX IF EXISTS ux_menu_diet_per_slot;
ALTER TABLE menus DROP COLUMN grade;

CREATE INDEX ix_menus_school_date_grade ON menus (school_id, menu_date, grade_id);

CREATE UNIQUE INDEX ux_menu_all_per_slot
    ON menus (school_id, menu_date, grade_id, meal_type)
    WHERE audience = 'ALL' AND status <> 'REJECTED';

CREATE UNIQUE INDEX ux_menu_diet_per_slot
    ON menus (school_id, menu_date, grade_id, meal_type, diet_group_id)
    WHERE diet_group_id IS NOT NULL AND status <> 'REJECTED';

-- ─── menu_templates.grade_id — NULLABLE (mirrors old nullable `grade`, trống = dùng chung
-- mọi khối, quyết định giữ nguyên từ V33) — KHÔNG raise nếu còn NULL sau backfill.
ALTER TABLE menu_templates ADD COLUMN grade_id UUID REFERENCES grades(id);

UPDATE menu_templates t
SET grade_id = g.id
FROM grades g
WHERE g.school_id = t.school_id
  AND t.grade IS NOT NULL
  AND g.name = 'Khối ' || substring(t.grade from '\d+');

ALTER TABLE menu_templates DROP COLUMN grade;
