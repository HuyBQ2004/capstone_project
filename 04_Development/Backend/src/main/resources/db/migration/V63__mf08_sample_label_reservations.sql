-- V63__mf08_sample_label_reservations.sql — In tem mẫu TRƯỚC cho nhiều ngày (quyết định của
-- Nam, 10/08/2026): bếp đặt trước mã/tem cho món cần lấy mẫu của các bữa sắp tới (lấy từ thực
-- đơn đã lập), in gộp tem trước, tới lúc lấy mẫu thật chỉ cần dán tem + bổ sung ảnh/cân nặng.
--
-- Cố tình KHÔNG đụng bảng `food_samples` (nhiều cột NOT NULL — sample_amount/container/
-- fridge_temp/collected_at/... — nới lỏng sẽ ảnh hưởng toàn bộ vòng đời BR-106..114 đã ổn định).
-- Mã đặt trước dùng CHUNG scope bộ đếm với `FoodSampleServiceImpl.resolveCreateCode()`
-- ("mf08_food_sample:" + yyyyMMdd) nên khi lấy mẫu thật gửi lại đúng mã này làm `id`, không
-- bao giờ trùng với mã một FoodSample khác đã/sẽ sinh cho cùng ngày (CodeGenerator chỉ tăng
-- bộ đếm theo scope, không biết/không cần biết bảng nào tiêu thụ mã — xem CodeGenerator.java).

CREATE TABLE sample_label_reservations (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(32)  NOT NULL,                          -- D1 — cùng khuôn SMP-yyyyMMdd-NN
    school_id    UUID         NOT NULL REFERENCES schools(id),
    collect_date DATE         NOT NULL,                          -- ngày DỰ KIẾN lấy mẫu
    meal_type    VARCHAR(16)  NOT NULL,
    dish_id      UUID         NOT NULL,                          -- MF05 dishes.id — ID thuần (ADR-M08-5)
    dish_name    VARCHAR(180) NOT NULL,                          -- snapshot lúc đặt trước
    status       VARCHAR(16)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','COLLECTED','CANCELLED')),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),            -- D9
    updated_at   TIMESTAMPTZ,
    created_by   VARCHAR(180),
    updated_by   VARCHAR(180)
);
CREATE UNIQUE INDEX uq_smplbl_school_code ON sample_label_reservations(school_id, code);
CREATE INDEX ix_smplbl_school_date ON sample_label_reservations(school_id, collect_date);
CREATE INDEX ix_smplbl_status ON sample_label_reservations(status);
