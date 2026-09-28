# Wireframe — Purchase Request Management (nhóm màn hình Raw Food Supplier, Mobile)

**Nguồn**: `03_Design/05_ScreenSpecifications/Mobile/RawFoodSupplier/` — 3 file Screen Specification (Draft).
**Công cụ sinh**: Thủ công — skill `wireframer`, vanilla HTML/JS SPA, khung phone-frame nhất quán với nhóm 01/04.
**File/Link**: `03_Design/06_Wireframes/Mobile/RawFoodSupplier/09_PurchaseRequestManagement.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (Dashboard → Purchase Request Screen → Detail → Confirm Quantity & Delivery Date).

---

## 1. Danh sách 3 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 09 | Purchase Requests | `09_PurchaseRequests.md` | Trên Mobile, ScreenFlow gộp 09+10 thành 1 luồng liên tục (xem ghi chú Screen Spec 10) |
| 10 | Purchase Request List | `10_PurchaseRequestList.md` | UC-139 |
| 11 | Purchase Request Detail | `11_PurchaseRequestDetail.md` | Accept dẫn UC-015 |

---

## 2. Đối chiếu với Screen Specification

### 09. Purchase Requests
- [x] Đủ field: bộ đếm nhanh theo trạng thái, điều hướng — đúng 4 dòng Business Logic.
- [x] Đúng Design System. [x] Loading tham chiếu, không dựng riêng.

### 10. Purchase Request List
- [x] Đủ field: Card (Order code, Status Chip, Source of request, Delivery date required), filter/search (rút gọn trong bản demo tĩnh, ghi chú mục 3) — đúng 10 dòng Business Logic.
- [x] Đúng Design System. [x] Success 2 dòng mẫu đủ 2 trạng thái.

### 11. Purchase Request Detail
- [x] Đủ field: Order number, Recipient, Delivery deadline, Ingredients List, nút Accept/Reject — đúng 10 dòng Business Logic.
- [x] Đúng Design System. [x] Reject dialog modal giữ nguyên [Giả định] về lý do bắt buộc.

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- 3 `<div class="screen">` + 1 modal overlay, khung `.phone` nhất quán các nhóm trước.
- Thanh filter/search của Screen Spec 10 (Web) được rút gọn trên bản demo Mobile tĩnh này (không có input tương tác thật, chỉ liệt kê 2 dòng mẫu) — đúng tinh thần Card list 1-bản-ghi-1-card theo `Component_Guidelines.md` §1, không mất field nào so với Business Logic.
- Nút Accept/Reject xếp dọc cuối màn hình (vùng ngón cái) theo `Layout_Guidelines.md` §3.

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn đang Draft, chưa qua QA của Nguyễn Phương Thảo.

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

Giống bản Web cùng nhóm (`Web/RawFoodSupplier/09_PurchaseRequestManagement.md` mục 5).
