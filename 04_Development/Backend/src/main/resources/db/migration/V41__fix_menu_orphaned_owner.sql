-- Sửa dữ liệu: một số bản ghi `menus` có `created_by_supplier_id IS NULL` (phát hiện
-- 06/08/2026 — 4/4 menu của trường SCH-MF03-0001 đều rơi vào trường hợp này, trong đó có
-- 2 bản DRAFT: MENU-2026-7874, MENU-2026-7326). `MenuServiceImpl.createDraft()` hiện tại LUÔN
-- gán `created_by_supplier_id = actor.partnerId()` (kiểm chứng 06/08/2026: tạo thử một menu
-- mới qua API trả về đúng `createdBySupplierId`), nên đây là dữ liệu CŨ hỏng từ trước, không
-- phải lỗi code hiện hành.
--
-- Hậu quả: `MenuAssembler.assertCatererOwnership()` so `menu.createdBySupplierId` với
-- `actor.partnerId()` bằng `Objects.equals()` — NULL không bao giờ bằng bất kỳ giá trị nào,
-- nên các menu này bị khoá SỬA/XOÁ/NỘP DUYỆT vĩnh viễn với MỌI tài khoản Service Caterers
-- (403 MENU-005), kể cả tài khoản lẽ ra sở hữu nó. Đây là nguyên nhân màn "Lịch thực đơn tuần"
-- báo "Access denied" khi Caterer bấm "Thêm thực đơn" vào ô ngày đã có menu orphan.
--
-- Chỉ backfill khi trường đó có ĐÚNG MỘT partner SERVICE_CATERER đang APPROVED — tránh gán
-- nhầm chủ sở hữu nếu sau này một trường có nhiều nhà thầu suất ăn cùng lúc; trường hợp
-- nhiều hơn 1 ứng viên thì bỏ qua, cần soát thủ công.
-- (array_agg(...))[1] thay vì MIN/MAX — Postgres không có aggregate MIN/MAX cho kiểu uuid;
-- HAVING COUNT(*) = 1 phía dưới đảm bảo mảng chỉ có đúng 1 phần tử nên lấy phần tử nào cũng
-- ra cùng một giá trị.
UPDATE menus m
SET created_by_supplier_id = sub.partner_id
FROM (
    SELECT school_id, (array_agg(partner_id))[1] AS partner_id
    FROM (
        SELECT DISTINCT m2.school_id, p.id AS partner_id
        FROM menus m2
        JOIN partners p ON p.partner_type = 'SERVICE_CATERER' AND p.status = 'APPROVED'
        WHERE m2.created_by_supplier_id IS NULL
    ) candidates
    GROUP BY school_id
    HAVING COUNT(*) = 1
) sub
WHERE m.school_id = sub.school_id
  AND m.created_by_supplier_id IS NULL;
