# Wireframe — Purchase Request Management (nhóm màn hình Raw Food Supplier)

**Nguồn**: `03_Design/05_ScreenSpecifications/Web/RawFoodSupplier/` — 3 file Screen Specification (Draft).
**Công cụ sinh**: Thủ công — skill `wireframer`, vanilla HTML/JS SPA.
**File/Link**: `03_Design/06_Wireframes/Web/RawFoodSupplier/09_PurchaseRequestManagement.html`

**Nguồn điều hướng**: `03_Design/04_ScreenFlow/11_Raw_Food_Supplier_Screen_Flow_Web.md` (Dashboard → Purchase Request List → Purchase Request Detail → Confirm Quantity & Delivery Date).

---

## 1. Danh sách 3 màn hình

| STT | Màn hình | File Screen Spec nguồn | Ghi chú |
| --- | --- | --- | --- |
| 09 | Purchase Requests | `09_PurchaseRequests.md` | Entry, bộ đếm nhanh [Giả định] |
| 10 | Purchase Request List | `10_PurchaseRequestList.md` | UC-139 |
| 11 | Purchase Request Detail | `11_PurchaseRequestDetail.md` | Bundled UC-139, Accept dẫn UC-015 |

Tổng cộng: **3 `<div class="screen">`** + 1 modal overlay (Reject dialog).

---

## 2. Đối chiếu với Screen Specification

### 09. Purchase Requests
- [x] Đủ field: Screen Title, bộ đếm nhanh theo trạng thái, điều hướng tới Purchase Request List — đúng 4 dòng Business Logic (đã đánh dấu [Giả định] cho bộ đếm ngay trong HTML).
- [x] Đúng Design System. [x] Loading ghi chú, không dựng riêng.

### 10. Purchase Request List
- [x] Đủ field: Back, Section Header, Order code, Status Badge, Source of request, Delivery date required, filter/search — đúng 10 dòng Business Logic.
- [x] Đúng Design System *(badge Awaiting confirmation = viền đứt nét, Delivery completed = viền liền)*.
- [x] Success (2 dòng mẫu đủ 2 trạng thái) + Empty tham chiếu Screen Spec (không dựng lặp lại ở đây vì đã minh hoạ empty pattern tương tự ở nhóm khác).

### 11. Purchase Request Detail
- [x] Đủ field: Order number, Recipient, Delivery deadline, List of ingredients to order (Section Header), Ingredients List Container, nút Accept/Reject — đúng 10 dòng Business Logic.
- [x] Đúng Design System *(nút Accept = btn-primary, Reject = btn-danger viền đậm)*.
- [x] Reject Dialog dựng dạng modal overlay, giữ nguyên cảnh báo [Giả định] về việc có bắt buộc nhập lý do hay không (chưa xác nhận trong Screen Spec nguồn).

---

## 3. Ghi chú kỹ thuật tuân thủ SKILL.md

- 3 `<div class="screen">` + 1 `.modal-overlay` (Reject dialog), điều hướng `showScreen`/`openModal`/`closeModal`.
- Grayscale/mono, wired-elements CDN, Patrick Hand, sketchy border, badge theo kiểu viền.
- Sidebar: chỉ nhóm "Đơn Mua hàng" active/click được trong file này.

## 4. Cảnh báo nguồn Draft

Toàn bộ Screen Specification nguồn đang Draft, chưa qua QA của Nguyễn Phương Thảo.

## 5. Trường hợp phát hiện Screen Spec nguồn thiếu thông tin/mâu thuẫn

1. **11_PurchaseRequestDetail.md**: hành động Reject không có mô tả rõ có bắt buộc nhập lý do hay không (Report 3 chỉ mô tả field cho Accept flow chi tiết, Reject chỉ xuất hiện trong ScreenFlow dạng mũi tên "Accept/Reject" không kèm field). Giữ nguyên [Giả định] trong modal Reject của Wireframe, cần Thảo xác nhận với BA (Huy) trước khi khoá.
2. **10_PurchaseRequestList.md / traceability_matrix.md**: Report 3 UC-139 Exceptions liệt kê MSG-018 (Inventory shortage) nhưng ngữ cảnh "xem danh sách" của Supplier khó khớp với MSG này (MSG-018 hợp lý hơn ở phía Service Caterers tạo Purchase Request) — Wireframe không hiển thị MSG-018 ở màn này, chỉ dùng MSG-004/005.
