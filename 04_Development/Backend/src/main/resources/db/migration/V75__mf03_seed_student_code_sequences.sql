-- ─────────────────────────────────────────────────────────────────────────────
-- V75 (13/08/2026, Nam) — nạp bộ đếm mã học sinh cho các lớp đang có dữ liệu.
--
-- Trước đây mã HS-<lớp>-<số> suy từ SĨ SỐ (`countByClassroomId() + 1`), không có bộ đếm nào
-- cả. StudentCodeAllocator nay chuyển sang code_sequences (D1, chống cấp trùng). Nếu không nạp
-- sẵn, bộ đếm của mọi lớp bắt đầu từ 1 và cấp đè lên mã đã tồn tại ngay từ học sinh kế tiếp.
--
-- Quy ước của CodeGenerator: code_sequences.next_value = SỐ SẼ CẤP TIẾP THEO (nó trả về
-- next_value - 1 sau khi +1). Vậy nạp = số lớn nhất đang dùng + 1.
--
-- Scope PHẢI khớp StudentCodeAllocator.scopeOf() = 'mf03_student:' || classroom_id.
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO code_sequences (scope, next_value)
SELECT 'mf03_student:' || s.classroom_id,
       MAX((regexp_match(s.code, '(\d+)$'))[1]::bigint) + 1
FROM students s
WHERE s.code ~ '\d+$'          -- bỏ qua mã không kết thúc bằng số (seed/nhập tay khuôn lạ)
GROUP BY s.classroom_id
ON CONFLICT (scope) DO UPDATE
    -- GREATEST: không bao giờ KÉO LÙI một bộ đếm đã chạy — lùi là cấp trùng.
    SET next_value = GREATEST(code_sequences.next_value, EXCLUDED.next_value);
