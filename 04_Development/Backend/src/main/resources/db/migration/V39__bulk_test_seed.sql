

-- ═══════════════ Xoá sạch dữ liệu BULK này (chạy tay khi không cần nữa) ═══════════════
-- DELETE FROM student_parents WHERE parent_user_id IN (SELECT id FROM user_accounts WHERE code LIKE 'U-BULK-%');
-- DELETE FROM students WHERE code LIKE 'HS-BULK-%';
-- DELETE FROM user_accounts WHERE code LIKE 'U-BULK-%';
-- DELETE FROM supplier_products WHERE code LIKE 'SP-BULK-%';
-- DELETE FROM inventory_batches WHERE code LIKE 'BATCH-BULK-%';
-- DELETE FROM classrooms WHERE code IN ('1A','1B','2A','2B','3B','4B','5A','5B');
