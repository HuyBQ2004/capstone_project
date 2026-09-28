-- ═══════════════════════════════════════════════════════════════════════════════
-- MF07 — Mẫu số 3 (QĐ 1246/QĐ-BYT) cột 7 "Dụng cụ chia, chứa đựng, che đậy,
-- bảo quản thức ăn" là ĐÁNH GIÁ Đạt/Không đạt, KHÔNG phải ô khai tên dụng cụ.
--
-- Vì sao đổi (Nam chốt 08/08/2026, rà UI): cột đang là VARCHAR(300) và màn kiểm
-- thực render một ô nhập tay với gợi ý "VD: Khay inox có nắp" — người kiểm gõ tên
-- dụng cụ vào. Biểu mẫu pháp lý cần biết dụng cụ có ĐẠT yêu cầu vệ sinh hay không;
-- tên dụng cụ không trả lời được câu đó, và không so sánh/thống kê được giữa các
-- phiên. Cùng khuôn với `sensory_pass` (boolean) + `action` (mô tả) đã có sẵn
-- trong chính bảng này.
--
-- Cách chuyển dữ liệu cũ: đã ghi CHỮ nghĩa là người kiểm đã xem và chấp nhận dụng
-- cụ đó ⇒ suy ra Đạt; để trống ⇒ cũng Đạt (mặc định an toàn cho bản ghi lịch sử,
-- KHÔNG tự ý kết luận "Không đạt" cho dữ liệu mình không đo được). Chữ cũ giữ
-- nguyên ở `utensils` làm ghi chú, không xoá — đây là biên bản pháp lý.
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE step3_rows
    ADD COLUMN utensils_pass BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN step3_rows.utensils_pass IS
    'Mẫu 3 cột 7 — dụng cụ chia/chứa đựng/che đậy ĐẠT yêu cầu vệ sinh (QĐ 1246)';

COMMENT ON COLUMN step3_rows.utensils IS
    'Ghi chú kèm theo cột 7 (tuỳ chọn) — VD loại dụng cụ, hoặc lý do khi Không đạt';

-- Không đạt thì phải có ảnh bằng chứng, cùng luật với `sensory_pass` (ck_s3_evidence).
ALTER TABLE step3_rows
    ADD CONSTRAINT ck_s3_utensils_evidence
        CHECK (utensils_pass = TRUE OR photo_count >= 1);
