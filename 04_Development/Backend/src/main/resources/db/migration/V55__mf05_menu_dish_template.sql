-- ═══════════════════════════════════════════════════════════════════════════════
-- V55__mf05_menu_dish_template.sql — ghi lại "món này vào ô từ MẪU THỰC ĐƠN nào".
--
-- Vấn đề: UI lịch thực đơn cho phép áp NHIỀU mẫu vào cùng một ô (ngày × khối × buổi),
-- mỗi mẫu là một chip riêng, bấm chip là gỡ mẫu đó ra. Nhưng nguồn gốc mẫu KHÔNG được lưu
-- ở đâu cả: `MenuBlockDish` không có `templateId`, `applyBlocksData()` bỏ qua hoàn toàn
-- mảng `templates[]` client gửi lên, và `MenuMapper.toBlocksData()` dựng lại block với
-- `templates = null`. Hệ quả đo được trên UI (07/08/2026):
--   · ô lịch chỉ hiện MỘT mục dù đã thêm 2–3 mẫu;
--   · chống trùng `blockEntries(cur).some(e => e.templateId === tpl.id)` không bao giờ khớp
--     ⇒ thêm cùng một mẫu nhiều lần vẫn lọt;
--   · `removeTemplate()` lọc `d.templateId !== templateId` với mọi `templateId` undefined
--     ⇒ KHÔNG gỡ món nào, nhưng vẫn toast "Đã gỡ thực đơn khỏi bữa" — UI nói dối.
--
-- Vì sao đặt ở `menu_dishes` chứ KHÔNG phải `menus.template_id`: một ô chứa N mẫu, quan hệ
-- 1-1 ở cấp `menus` sai arity ngay từ đầu. Nguồn gốc là thuộc tính của TỪNG MÓN trong ô.
--
-- NULL = món thêm tay (không qua mẫu) — hợp lệ và phổ biến, nên không NOT NULL.
-- ON DELETE SET NULL: xoá mẫu KHÔNG được xoá món khỏi thực đơn đã lập, chỉ mất dấu nguồn gốc.
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE menu_dishes
  ADD COLUMN template_id UUID REFERENCES menu_templates(id) ON DELETE SET NULL;

CREATE INDEX ix_menu_dishes_template ON menu_dishes(template_id) WHERE template_id IS NOT NULL;
