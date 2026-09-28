-- ─────────────────────────────────────────────────────────────────────────────
-- V77 (13/08/2026, Nam) — dọn dòng mẫu biểu kiểm thực bị trùng + chặn tái diễn.
--
-- BỐI CẢNH: chốt bước (`InspectionServiceImpl.applyStep1..3`) xoá sạch dòng của bước rồi chèn
-- lại. Hai request chạy song song (double-click, hoặc mạng chậm nên bấm lại) đều đọc trạng thái
-- cũ, đều xoá, rồi đều chèn ⇒ sổ có HAI bộ dòng trùng nhau y hệt, CÙNG `row_order`.
--
-- Đo trên production: phiên INSP-20260813-B có 18 dòng cho 9 nguyên liệu; bản in Mẫu số 1
-- (QĐ 1246/QĐ-BYT) lặp mỗi nguyên liệu 2 lần — sai hồ sơ pháp lý.
--
-- Đã vá 2 lớp ở code: nút chốt bước khoá khi request đang bay (client), và khoá ghi hàng phiên
-- khi submitStep (server). Migration này là lớp thứ 3 — DB không cho phép tồn tại 2 dòng cùng
-- (session_id, row_order) nữa, nên dù có đường ghi nào bị bỏ sót thì dữ liệu vẫn không hỏng.
--
-- THỨ TỰ BẮT BUỘC: dọn trước, thêm ràng buộc sau (thêm trước sẽ vỡ vì dữ liệu đang trùng).
-- ─────────────────────────────────────────────────────────────────────────────

-- ── 1. Dọn: giữ dòng có ctid nhỏ nhất trong mỗi cụm (session_id, row_order) ──
-- Các dòng trùng giống hệt nhau về nội dung nên giữ bản nào cũng như nhau.
DELETE FROM step1_fresh_rows a
WHERE a.ctid <> (SELECT min(b.ctid) FROM step1_fresh_rows b
                 WHERE b.session_id = a.session_id AND b.row_order = a.row_order);

DELETE FROM step1_dry_rows a
WHERE a.ctid <> (SELECT min(b.ctid) FROM step1_dry_rows b
                 WHERE b.session_id = a.session_id AND b.row_order = a.row_order);

DELETE FROM step2_rows a
WHERE a.ctid <> (SELECT min(b.ctid) FROM step2_rows b
                 WHERE b.session_id = a.session_id AND b.row_order = a.row_order);

DELETE FROM step3_rows a
WHERE a.ctid <> (SELECT min(b.ctid) FROM step3_rows b
                 WHERE b.session_id = a.session_id AND b.row_order = a.row_order);

-- ── 2. Chặn tái diễn ở tầng DB ──
ALTER TABLE step1_fresh_rows ADD CONSTRAINT uq_step1_fresh_session_order UNIQUE (session_id, row_order);
ALTER TABLE step1_dry_rows   ADD CONSTRAINT uq_step1_dry_session_order   UNIQUE (session_id, row_order);
ALTER TABLE step2_rows       ADD CONSTRAINT uq_step2_session_order       UNIQUE (session_id, row_order);
ALTER TABLE step3_rows       ADD CONSTRAINT uq_step3_session_order       UNIQUE (session_id, row_order);
