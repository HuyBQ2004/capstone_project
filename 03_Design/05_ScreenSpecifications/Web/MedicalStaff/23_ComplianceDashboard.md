# Screen Spec — Compliance Dashboard

**Actor**: Medical Staff (màn hình dùng chung với School Administration — file này viết riêng theo góc nhìn Medical Staff) | **UC liên quan**: UC-027 (Report 3 §3.2, dòng 3469–3483) | **Status**: Draft
**BR liên quan**: BR-091, BR-097, BR-100, BR-104
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-093 (Warning — cảnh báo điểm checklist thấp)

> **Cập nhật 05/07/2026**: đội dự án quyết định **bỏ field "Cảnh báo Kitchen Quarantine"** (field #6 cũ) khỏi màn hình này. Field đó vốn trích `BR-105` (ghost-citation — canonical là quy tắc lưu mẫu thực phẩm, không liên quan) và `MSG-024` (theo `error_message.md`, mã này thuộc module Incident Management, dùng cho BR-149 — cách ly lô nguyên liệu khi có sự cố, không phải ngưỡng điểm checklist). Quyết định descope: điểm checklist <60% (UC-014) **không còn khóa vận hành bếp** — chỉ tự động cảnh báo School Administration (xem `business_rules.md` §12, ghi chú sau BR-208). Đã thay field #6 bằng field #4b — Alert cảnh báo điểm thấp (không phải banner khóa cố định). Nếu về sau có một sự cố ATTP thực sự kích hoạt cách ly **lô nguyên liệu** (BR-149), cảnh báo đó thuộc phạm vi UC-041 (Incident Management), không phải màn hình này.
>
> **Quyết định 12/07/2026 (rà soát ảnh chụp màn hình thực tế, Hình 6)**: `compliance/page.tsx` hiện có thêm 1 card "Nhiệt độ kho & tủ lưu mẫu (BR-089/BR-110)" — **card này KHÔNG có trong đặc tả màn hình gốc** (không thuộc danh sách field dưới đây), phát sinh ngoài phạm vi khi triển khai code, đồng thời còn dẫn BR-089 đã Deprecated. Quyết định: **KHÔNG đưa card này vào đặc tả** — dữ liệu nhiệt độ tủ lưu mẫu (BR-110) đã có chỗ đúng phạm vi tại `15_ViewFoodSafetyReportsScreen.md` (nhóm báo cáo "Refrigerator Temperature Logs"), không cần lặp lại ở Compliance Dashboard. Xem `review_screen_medical_staff.md` Mục 16.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Trạng thái real-time 3 bước kiểm tra hôm nay | Widget trạng thái | — | BR-091 | UC-027 bước 2 |
| 2 | Điểm checklist vệ sinh hàng ngày | Widget số liệu | — | BR-100, BR-104 | UC-027 bước 3 |
| 3 | Ảnh xác minh (verification photos) | Ảnh preview | — | — | UC-027 bước 4 |
| 4 | Cảnh báo bước kiểm tra thất bại/quá hạn | Alert | Hiện khi có non-conformance | BR-097 | AF1 |
| 4b | Cảnh báo điểm checklist <60% *(thay thế field Quarantine cũ, 05/07/2026)* | Alert (warning, không phải banner cố định) | Hiện khi điểm gần nhất <60%; đã tự động gửi cảnh báo School Administration | — | Không khóa vận hành bếp, không chặn phân phát bữa ăn — descope so với AF1 gốc UC-014 |
| 5 | Nút "Xem chi tiết cảnh báo" | Button | — | — | |

### Trạng thái màn hình
- Loading: `Skeleton` cho các widget số liệu.
- Empty: "Chưa có dữ liệu sản xuất hôm nay".
- Error: MSG-004, MSG-005; điểm <60% → Alert cảnh báo (không phải banner cố định, MSG-093).
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Food Compliance & Monitoring Board" trên Dashboard (Web).
**Bố cục**: Content Area grid 12 cột — widget trạng thái 3 bước + điểm checklist hàng trên, ảnh xác minh + cảnh báo (non-conformance, điểm thấp) hàng dưới.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Compliance & Monitoring Board" trên Dashboard (Mobile).
**Bố cục**: 1 cột, widget xếp dọc dạng Card, Alert cảnh báo (không phải banner cố định) nếu có non-conformance/điểm thấp.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Compliance Dashboard, nền tảng Web, actor Medical Staff.
Bố cục: grid 12 cột, trạng thái 3 bước kiểm tra, điểm checklist, ảnh xác minh,
cảnh báo bước thất bại, Alert cảnh báo điểm checklist <60% (không phải banner khóa).
Áp dụng Design System: warning #B45309 cho Alert điểm thấp/bước thất bại.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3469–3483 (UC-027).
- business_rules.md (BR-091, BR-097, BR-100, BR-104; xem §12 ghi chú sau BR-208 về quyết định descope khóa bếp).
- error_message.md (MSG-004, MSG-005; MSG-093 §3 Warning; MSG-024 không áp dụng cho màn này — thuộc Incident Management).

**Ghi chú màn hình dùng chung**: giống bản Mobile — **đã hợp nhất** với bản `Web/SchoolAdministration/18_ComplianceDashboard.md` ở bước review (mục 6 của `00_Team_Work_Assignment_Plan.md`, 2026-07-01) — field dữ liệu #1-#4 (3-bước realtime, điểm vệ sinh, ảnh xác minh, cảnh báo non-conformance) nay đồng nhất ở cả 2 bản; hành động riêng theo actor (School Administration: "Ký xác nhận"; Medical Staff: "Xem chi tiết cảnh báo") giữ nguyên khác biệt. **Cập nhật 05/07/2026**: banner Kitchen Quarantine (MSG-024/BR-105) đã bị gỡ khỏi cả 2 bản — xem ghi chú đầu file; mã cảnh báo thay thế: **MSG-093**.

**Cập nhật 05/07/2026 — bổ sung US-09 (BR-209, UC-185)**: xem `28_SendComplianceReportDialog.md` — Medical Staff nay có thể chủ động soạn/gửi báo cáo tuân thủ ATTP (Daily/Weekly) cho School Administration từ menu, không cần chỉ dựa vào School Administration tự vào xem màn Compliance Dashboard.
