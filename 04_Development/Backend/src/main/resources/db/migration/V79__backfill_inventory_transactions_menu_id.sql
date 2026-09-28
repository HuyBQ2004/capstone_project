-- 14/08/2026 (Nam) — backfill cho bug đã sửa ở `InventoryService.createIssueSlip()`: trước đây
-- MỌI dòng xuất kho của một phiếu xuất (dù phiếu gộp nhiều khối/nhiều menu cùng bữa) đều bị gán
-- CỨNG `menu_id = slip.menu_ids[0]` — sai với những dish thuộc menu KHÁC trong cùng phiếu (VD
-- phiếu gộp khối 1+2+3, món của khối 2/3 lại bị ghi nhận thuộc menu khối 1). Bản sửa dùng
-- `menu_dishes` để tra đúng menu theo từng dish; migration này áp lại đúng cách tra đó cho các
-- dòng ĐÃ SINH TRƯỚC bản sửa.
--
-- Idempotent: chỉ đổi dòng có menu_id SAI (khác kết quả tra lại) — chạy lại nhiều lần vô hại.
-- Cùng hạn chế như code service (`dishToMenuMap` dùng putIfAbsent): nếu một dish xuất hiện ở
-- NHIỀU menu trong cùng phiếu (hiếm — món giống hệt dùng chung ở nhiều khối), kết quả chọn 1
-- trong số đó không xác định thứ tự — chấp nhận được vì đây là backfill dữ liệu lịch sử, không
-- phải đường ghi mới.
UPDATE inventory_transactions t
SET menu_id = md.menu_id
FROM issue_slips s
JOIN menu_dishes md ON md.menu_id = ANY (s.menu_ids)
WHERE t.issue_slip_id = s.id
  AND md.dish_id = t.dish_id
  AND t.dish_id IS NOT NULL
  AND t.menu_id IS DISTINCT FROM md.menu_id;
