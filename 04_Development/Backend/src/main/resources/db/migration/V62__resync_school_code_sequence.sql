-- ═══════════════════════════════════════════════════════════════════════════════
-- Đồng bộ lại `code_sequences` (scope 'schools') với mã LỚN NHẤT đang thật sự tồn
-- tại trong `schools` (10/08/2026).
--
-- Đo thật: đăng ký trường mới (không tự gửi `id`) bị 500 —
-- `duplicate key value violates unique constraint "schools_code_key"`, Key
-- (code)=(SCH-0001) already exists. `SchoolService.register()` gọi
-- `CodeGenerator.nextSchoolCode()` sinh lại đúng "SCH-0001" dù mã đó đã tồn tại
-- thật trong `schools` — nghĩa là bộ đếm ở `code_sequences` (độc lập với bảng
-- `schools`) đang lệch: hoặc chưa từng chạy, hoặc bị reset về đầu trong khi
-- `schools` vẫn còn dữ liệu cũ. Check trùng mã ở tầng ứng dụng
-- (`schoolRepository.existsByCode`) chỉ áp dụng khi CLIENT TỰ GỬI mã — không bắt
-- được trường hợp mã TỰ SINH bị trùng, nên request rơi thẳng xuống DB và vỡ
-- constraint (không có bug logic ở SchoolService/CodeGenerator, chỉ là dữ liệu
-- hai bảng lệch nhau).
--
-- GREATEST + ON CONFLICT: an toàn chạy lại nhiều lần, không bao giờ kéo bộ đếm
-- LÙI nếu nó đã tiến xa hơn giá trị tính từ dữ liệu hiện có.
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO code_sequences (scope, next_value)
SELECT 'schools', COALESCE(MAX((substring(code from 5))::int), 0) + 1
FROM schools
WHERE code ~ '^SCH-[0-9]+$'
ON CONFLICT (scope) DO UPDATE
SET next_value = GREATEST(code_sequences.next_value, EXCLUDED.next_value);
