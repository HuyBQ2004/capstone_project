-- =========================================================
-- MF05 — Menu & Diet Management
-- Nguồn: TDS-MF05_Backend.md v1.2 §5.2 (M05-D01..D16)
--        mock-data.ts (MenuItem/Dish/MenuTemplate/DietGroup)
--        data_model.puml package "Thực đơn & Món ăn" (đã trừ MenuVariant — CF-4/ADR-M05-01)
-- KHÔNG có special_meal_requests: bảng đó thuộc MF03, đã tạo ở V4 (M05-D04).
-- 4 cột audit created_at/updated_at/created_by/updated_by theo khuôn §0-BIS (D9) —
-- entity KHÔNG khai lại trong Java, nhưng DDL BẮT BUỘC tạo đủ.
--
-- Thứ tự tạo bảng theo phụ thuộc FK: diet_groups, dishes (danh mục, không FK vào menus)
-- → menus (FK diet_groups) → dish_ingredients/menu_dishes (FK dishes/menus)
-- → menu_dish_substitutes (FK menu_dishes/dishes) → menu_templates → menu_template_dishes.
-- =========================================================

-- dishes / diet_groups là DANH MỤC TOÀN HỆ THỐNG (không gắn school_id) ⇒ UNIQUE(code) chứ
-- không phải UNIQUE(school_id, code). D1 chỉ đòi `code` là khoá công khai, không đòi tenant hoá.
CREATE TABLE diet_groups (
    id                           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code                         VARCHAR(32)  NOT NULL UNIQUE,   -- VEGETARIAN | HALAL | DIABETIC ...
    label                        VARCHAR(64)  NOT NULL,
    match_diet                   VARCHAR(64),
    exclude_allergens            JSONB        NOT NULL DEFAULT '[]',
    exclude_ingredient_keywords  JSONB        NOT NULL DEFAULT '[]',
    note                         VARCHAR(300),
    created_at                   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                   TIMESTAMPTZ,
    created_by                   VARCHAR(180),
    updated_by                   VARCHAR(180)
);
-- ⚠️ `match_diet` khớp với `special_meal_requests.dietary_type` của MF03 (danh mục MỞ, BR-220 —
-- M05-D05). KHÔNG khai CHECK/enum trên cột này: danh mục là CẤU HÌNH ở `StandardDietCatalog`.

CREATE TABLE dishes (
    id                     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code                   VARCHAR(64)  NOT NULL UNIQUE,   -- slug: "ca-basa-sot-ca-chua"
    name                   VARCHAR(160) NOT NULL,
    category               VARCHAR(24)  NOT NULL
                           CHECK (category IN ('MON_CHINH','MON_MAN','CANH','RAU','BUA_PHU','TRANG_MIENG')),
    kcal                   NUMERIC(8,2) NOT NULL,
    protein                NUMERIC(8,2) NOT NULL,
    fat                    NUMERIC(8,2) NOT NULL,
    carb                   NUMERIC(8,2) NOT NULL,
    manual_allergens       JSONB        NOT NULL DEFAULT '[]',
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    default_substitute_id  UUID         REFERENCES dishes(id),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),   -- D9
    updated_at             TIMESTAMPTZ,                           -- D9
    created_by             VARCHAR(180),                          -- D9
    updated_by             VARCHAR(180),                          -- D9
    CONSTRAINT ck_dish_no_self_substitute CHECK (default_substitute_id <> id)
);

CREATE TABLE menus (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code                    VARCHAR(32)  NOT NULL,                                -- D1 — "MENU-2026-30"
    school_id               UUID         NOT NULL REFERENCES schools(id),
    week_label              VARCHAR(64)  NOT NULL,
    name                    VARCHAR(160) NOT NULL,
    grade                   VARCHAR(16)  NOT NULL,
    menu_date               DATE         NOT NULL,
    meal_type               VARCHAR(16)  NOT NULL
                            CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK')),   -- common/enums/MealType (D6)
    status                  VARCHAR(24)  NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT','PENDING_APPROVAL','APPROVED','PUBLISHED','REJECTED')),
    audience                VARCHAR(32)  NOT NULL DEFAULT 'ALL',
    diet_group_id           UUID         REFERENCES diet_groups(id),
    estimated_cost          NUMERIC(12,2),
    cost_ceiling            NUMERIC(12,2),
    submitted_at            TIMESTAMPTZ,
    servings                INT,
    created_by_supplier_id  UUID         REFERENCES partners(id),                 -- D9 — chủ sở hữu (nghiệp vụ)
    approved_by_user_id     UUID         REFERENCES user_accounts(id),            -- D9
    approval_signature      VARCHAR(256),
    approved_at             TIMESTAMPTZ,
    reject_reason           VARCHAR(1000),
    reason                  VARCHAR(500),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),   -- D9 BaseEntity
    updated_at              TIMESTAMPTZ,                           -- D9 (KHÔNG NOT NULL/DEFAULT)
    created_by              VARCHAR(180),                          -- D9
    updated_by              VARCHAR(180),                          -- D9
    CONSTRAINT uq_menus_school_code UNIQUE (school_id, code),      -- D1
    -- BR-068: đã duyệt/công bố thì bắt buộc có người duyệt
    CONSTRAINT ck_menu_approved_has_approver
        CHECK (status NOT IN ('APPROVED','PUBLISHED') OR approved_by_user_id IS NOT NULL),
    -- BR-067 + luật semantic-audit "DA_DUYET_MA_THIEU_NGUOI_DUYET"
    CONSTRAINT ck_menu_rejected_has_reason
        CHECK (status <> 'REJECTED' OR reject_reason IS NOT NULL),
    -- audience khác ALL thì phải chỉ rõ nhóm chế độ ăn (ADR-M05-01)
    CONSTRAINT ck_menu_audience_dietgroup
        CHECK (audience = 'ALL' OR diet_group_id IS NOT NULL)
);
CREATE INDEX ix_menus_school_date_grade ON menus (school_id, menu_date, grade);
CREATE INDEX ix_menus_status            ON menus (status);
CREATE INDEX ix_menus_audience          ON menus (audience);
-- Một khối chỉ có ĐÚNG MỘT thực đơn chung cho mỗi (ngày, buổi)
CREATE UNIQUE INDEX ux_menu_all_per_slot
    ON menus (school_id, menu_date, grade, meal_type)
    WHERE audience = 'ALL' AND status <> 'REJECTED';

CREATE TABLE dish_ingredients (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dish_id          UUID         NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
    -- M05-D15: catalog_ingredients là bảng của MF06, CHƯA TỒN TẠI ⇒ lưu mã, KHÔNG khai FK.
    -- MF06 tự ALTER TABLE ... ADD CONSTRAINT khi bảng của họ lên (Huy).
    ingredient_code  VARCHAR(64),                                 -- NULL = ngoài danh mục
    name             VARCHAR(160) NOT NULL,
    quantity         VARCHAR(32)  NOT NULL,
    unit             VARCHAR(16)  NOT NULL DEFAULT 'g',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    created_by       VARCHAR(180),
    updated_by       VARCHAR(180)
);
CREATE INDEX ix_dish_ing_dish ON dish_ingredients (dish_id);

CREATE TABLE menu_dishes (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_id         UUID         NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    dish_id         UUID         NOT NULL REFERENCES dishes(id),
    serving_order   INT          NOT NULL DEFAULT 0,
    portions        INT          NOT NULL DEFAULT 0,
    is_special_dish BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(180),
    updated_by      VARCHAR(180),
    CONSTRAINT uq_menu_dish UNIQUE (menu_id, dish_id),
    CONSTRAINT ck_menu_dish_portions CHECK (portions >= 0)
);

CREATE TABLE menu_dish_substitutes (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_dish_id       UUID        NOT NULL REFERENCES menu_dishes(id) ON DELETE CASCADE,
    substitute_dish_id UUID        NOT NULL REFERENCES dishes(id),
    portions           INT         NOT NULL DEFAULT 0,
    note               VARCHAR(300),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ,
    created_by         VARCHAR(180),
    updated_by         VARCHAR(180)
);

CREATE TABLE menu_templates (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(32)  NOT NULL,
    school_id  UUID         NOT NULL REFERENCES schools(id),
    name       VARCHAR(160) NOT NULL,
    meal_type  VARCHAR(16)  NOT NULL CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK')),
    audience   VARCHAR(32)  NOT NULL DEFAULT 'ALL',
    note       VARCHAR(300),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(180),
    updated_by VARCHAR(180),
    CONSTRAINT uq_menu_templates_school_code UNIQUE (school_id, code)   -- D1
);

CREATE TABLE menu_template_dishes (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id   UUID        NOT NULL REFERENCES menu_templates(id) ON DELETE CASCADE,
    dish_id       UUID        NOT NULL REFERENCES dishes(id),
    serving_order INT         NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ,
    created_by    VARCHAR(180),
    updated_by    VARCHAR(180),
    CONSTRAINT uq_template_dish UNIQUE (template_id, dish_id)
);

-- ❌ KHÔNG tạo `special_meal_requests` ở đây — M05-D04.
--    Bảng đã tồn tại từ V4__mf03_student_parent.sql (MF03, đã merge vào dev).
--    `CREATE TABLE` lại = Flyway đỏ ngay lần migrate đầu.
--    MF05 chỉ ĐỌC qua SpecialMealLookupPort (mf03/port/, tạm thời native-query fallback trong
--    mf05/service — xem TDS §2.5 mục 7), không import entity/repository MF03.
