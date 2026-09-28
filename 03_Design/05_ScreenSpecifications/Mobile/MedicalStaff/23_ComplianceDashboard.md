# Screen Spec — Compliance Dashboard

**Actor**: Medical Staff (màn hình dùng chung với School Administration — xem `03_Design/05_ScreenSpecifications/Web|Mobile/SchoolAdministration/` cho bản School Administration; file này viết riêng theo góc nhìn Medical Staff theo `14_Screen_Authorization_Matrix.md` khối 3, dòng "Compliance Dashboard") | **UC liên quan**: UC-027 (Report 3 §3.2, dòng 3469–3483 — primary actor gốc là School Administration, Medical Staff dùng chung theo cột `x`) | **Status**: Draft
**BR liên quan**: BR-091 (nhiệt độ giữ ấm ≥60°C, giám sát trạng thái Step 3), BR-097 (tích hợp quản lý sự cố khi kiểm tra thất bại nghiêm trọng), BR-100 (điểm vệ sinh hàng ngày), BR-104 (điểm vệ sinh đầu bếp)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error), MSG-093 (Warning — cảnh báo điểm checklist thấp)

> **Cập nhật 05/07/2026**: đội dự án quyết định **bỏ field "Cảnh báo Kitchen Quarantine"** (field #6 cũ). Field đó trích `BR-105` (ghost-citation, canonical là quy tắc lưu mẫu thực phẩm) và `MSG-024` (theo `error_message.md`, thuộc module Incident Management, dùng cho BR-149 — cách ly lô nguyên liệu khi có sự cố, không phải ngưỡng điểm checklist). Điểm checklist <60% (UC-014) **không còn khóa vận hành bếp** — chỉ tự động cảnh báo School Administration (xem `business_rules.md` §12, ghi chú sau BR-208). Đã thay bằng field #4b — Alert cảnh báo điểm thấp (không phải banner cố định).
>
> **Quyết định 12/07/2026 (rà soát ảnh chụp màn hình thực tế, Hình 6)**: bản Web (`compliance/page.tsx`) hiện có thêm 1 card "Nhiệt độ kho & tủ lưu mẫu (BR-089/BR-110)" — **card này KHÔNG có trong đặc tả màn hình gốc**, phát sinh ngoài phạm vi khi triển khai code, đồng thời còn dẫn BR-089 đã Deprecated. Quyết định: **KHÔNG đưa card này vào đặc tả** (áp dụng cho cả Web lẫn Mobile) — dữ liệu nhiệt độ tủ lưu mẫu (BR-110) đã có chỗ đúng phạm vi tại `15_ViewFoodSafetyReportsScreen.md`. Xem `review_screen_medical_staff.md` Mục 16.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Trạng thái real-time 3 bước kiểm tra hôm nay (Step 1/2/3) | Widget trạng thái | — | BR-091 | UC-027 bước 2 |
| 2 | Điểm checklist vệ sinh hàng ngày do Service Caterers nộp | Widget số liệu | — | BR-100, BR-104 | UC-027 bước 3 |
| 3 | Ảnh xác minh (verification photos) | Ảnh preview | — | — | UC-027 bước 4 |
| 4 | Cảnh báo bước kiểm tra thất bại/quá hạn | Alert/Banner | Hiện khi có non-conformance (BR-097) | BR-097 | AF1 — điều hướng chi tiết non-conformance |
| 4b | Cảnh báo điểm checklist <60% *(thay thế field Quarantine cũ, 05/07/2026)* | Alert (warning, không phải banner cố định) | Hiện khi điểm gần nhất <60%; đã tự động gửi cảnh báo School Administration | — | Không khóa vận hành bếp, không chặn phân phát bữa ăn |
| 5 | Nút "Xem chi tiết cảnh báo" | Button | — | — | Điều hướng liên kết corrective action request liên quan |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` cho các widget số liệu real-time.
- Empty: nếu chưa có phiên sản xuất nào trong ngày → "Chưa có dữ liệu sản xuất hôm nay".
- Error: MSG-004, MSG-005; điểm <60% → Alert cảnh báo (không phải banner cố định, MSG-093).
- Success: không áp dụng (màn hình xem, hành động "sign off" thuộc về School Administration ở bản của họ).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Food Compliance & Monitoring Board" trên Dashboard; điều hướng tiếp sang `16_ViewSchoolPolicyScreen.md`, `15_ViewFoodSafetyReportsScreen.md`.
**Bố cục**: 1 cột, các widget xếp dọc dạng Card (trạng thái 3 bước, điểm checklist, ảnh xác minh), Alert cảnh báo (không phải banner cố định) nếu có non-conformance/điểm thấp.

## Platform Layout — Web
**Entry/Exit points**: Vào từ shortcut "Food Compliance & Monitoring Board" trên Dashboard (Web).
**Bố cục**: `Layout_Guidelines.md` mục 1.1 — Content Area grid 12 cột: widget trạng thái 3 bước + điểm checklist chiếm hàng trên (`col-span-6` mỗi widget), ảnh xác minh + cảnh báo non-conformance/điểm thấp ở hàng dưới.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Compliance Dashboard, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: trạng thái 3 bước kiểm tra hôm nay, điểm checklist vệ sinh hàng ngày,
ảnh xác minh, cảnh báo bước kiểm tra thất bại/quá hạn, Alert cảnh báo điểm checklist <60%
(không phải banner khóa).
Áp dụng Design System: warning #B45309 cho các Alert cảnh báo.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3469–3483 (UC-027).
- business_rules.md (BR-091, BR-097, BR-100, BR-104; xem §12 ghi chú sau BR-208 về quyết định descope khóa bếp).
- error_message.md (MSG-004, MSG-005; MSG-093 §3 Warning; MSG-024 không áp dụng cho màn này — thuộc Incident Management).
- `14_Screen_Authorization_Matrix.md` khối 3 (dòng Compliance Dashboard, cột MS có `x`).

**Ghi chú màn hình dùng chung**: theo mục 5 của `00_Team_Work_Assignment_Plan.md`, đây là màn hình dùng chung School Administration + Medical Staff — bản School Administration (do Nam phụ trách) có thể có thêm hành động "sign off" (UC-027 bước 5) mà Medical Staff không có; phần field hiển thị dữ liệu (widget số liệu) sẽ được hợp nhất ở bước review (mục 6 của kế hoạch).

**Cập nhật 05/07/2026 — bổ sung US-09 (BR-209, UC-185)**: xem `28_SendComplianceReportDialog.md` — Medical Staff nay có thể chủ động soạn/gửi báo cáo tuân thủ ATTP (Daily/Weekly) cho School Administration, không cần chỉ dựa vào School Administration tự vào xem màn này.
