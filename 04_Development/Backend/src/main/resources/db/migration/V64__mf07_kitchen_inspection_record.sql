-- BR-221 mở rộng (10/08/2026) — biên bản sự việc kiểm tra bếp, tự sinh khi Medical Staff
-- hoàn thành checklist (POST /kitchen-inspections), Medical Staff sửa được trước khi
-- Service Caterers xác nhận (PATCH /kitchen-inspections/{code}/record).
ALTER TABLE kitchen_inspections ADD COLUMN record_content TEXT;
