-- ═══════════════════════════════════════════════════════════════════════════════
-- V68__kitchen_inspection_item_result_id.sql — surrogate id cho checklist_item_results.
--
-- `checklist_item_results` chỉ có PK ghép (inspection_id, item_id) — không có cột id đơn
-- lẻ nào để `inspection_evidences.owner_id` (kiểu UUID, 1 cột) tham chiếu tới ĐÚNG MỘT
-- dòng kết quả. Trước 11/08/2026, ảnh bằng chứng của "Kiểm tra an toàn bếp" (UC-014) chỉ
-- được ĐẾM (`photo_count`), không có file thật nào lưu — trong khi `inspection_evidences`
-- (V12) đã có sẵn owner_type='KITCHEN_ITEM' dự trù cho đúng trường hợp này nhưng chưa
-- từng được ghi. Thêm cột `id` (không đổi PK ghép hiện có, không phá query/FK nào khác)
-- để mỗi dòng kết quả có 1 UUID riêng dùng làm owner_id khi upload ảnh thật.
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE checklist_item_results ADD COLUMN id UUID NOT NULL DEFAULT gen_random_uuid();
CREATE UNIQUE INDEX ux_checklist_item_results_id ON checklist_item_results (id);
