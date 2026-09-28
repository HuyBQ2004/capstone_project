# Screen Spec — Compliance Dashboard

**Actor**: School Administration | **UC liên quan**: UC-027 (Report 3 §3.2 "Daily Food Safety Monitoring") | **Status**: Draft
**BR liên quan**: BR-091 (nhiệt độ giữ ấm — theo dõi thời gian thực), BR-097 (tích hợp sự cố), BR-100 (điểm vệ sinh hàng ngày), BR-104 (điểm vệ sinh đầu bếp), BR-149 (tự động Quarantine **lô nguyên liệu** khi có sự cố ATTP — xem ghi chú phạm vi)
**MSG liên quan**: MSG-004, MSG-005, MSG-093 (cảnh báo điểm checklist thấp — Warning)

> **Đã hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)**: phần field hiển thị dữ liệu đối chiếu với bản `Web/MedicalStaff/23_ComplianceDashboard.md`; giữ nguyên hành động riêng của School Administration (nút Ký xác nhận).
>
> **Cập nhật 05/07/2026 — bỏ banner Kitchen Quarantine gắn với điểm checklist**: field #5 cũ ("Banner Kitchen Quarantine... BR-105, BR-149... MSG-024") trộn lẫn 2 cơ chế khác nhau: (a) ngưỡng điểm checklist UC-014 <60% (trích `BR-105`, ghost-citation — canonical là quy tắc lưu mẫu thực phẩm) và (b) cách ly **lô nguyên liệu cụ thể** khi có sự cố ATTP thực sự (BR-149, đúng phạm vi Incident Management). Đội dự án quyết định: (a) **không còn khóa vận hành bếp** khi điểm checklist thấp — chỉ tự động cảnh báo School Administration (xem `business_rules.md` §12, ghi chú sau BR-208); (b) BR-149 **giữ nguyên, không đổi** — nếu có sự cố ATTP kích hoạt cách ly lô nguyên liệu, đó là thông tin thuộc UC-041 (Incident Management), không hiển thị ở Compliance Dashboard này nữa để tránh nhầm 2 cơ chế. `MSG-024` không còn dùng cho màn này (theo `error_message.md`, mã này thuộc module Incident Management). Mã thay thế đã cấp: **`MSG-093`** (Warning), xem `error_message.md` §3.
>
> **Cập nhật 05/07/2026 — bổ sung US-09 (BR-209, UC-185)**: Medical Staff hiện có thể chủ động soạn và gửi báo cáo tuân thủ ATTP (Daily/Weekly) cho School Administration, thay vì chỉ dựa vào School Administration tự vào xem màn này (pull). Đây là kênh bổ sung — School Administration vẫn có thể tự xem Compliance Dashboard khi màn hình này được triển khai thực tế (hiện Screen Spec đã có nhưng **chưa có route/trang thực thi phía frontend School Administration** — xem `03_Design/08_FrontEnd/Web/review/review_screen_medical_staff.md` mục 3.9). Báo cáo chủ động từ Medical Staff không thay thế màn hình này.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Trạng thái 3-bước kiểm tra hôm nay theo thời gian thực | Card/Progress theo lô sản xuất | Step 1/2/3, cập nhật realtime | BR-091 | |
| 2 | Điểm vệ sinh hàng ngày của bếp | Card số liệu | — | BR-100, BR-104 | |
| 3 | Cảnh báo bước kiểm tra thất bại/quá hạn | Alert (danger) | Hiện link tới yêu cầu CAPA đang mở | BR-097 | AF1 UC-027 |
| 4 | Ảnh xác minh (verification photos) | Gallery rút gọn | — | — | |
| 4b | Cảnh báo điểm checklist <60% *(thay thế field Quarantine cũ, 05/07/2026)* | Alert (warning, không phải banner cố định) | Hiện khi điểm gần nhất <60% | — | Không khóa vận hành bếp, không chặn phân phát bữa ăn |
| 5 | Nút "Ký xác nhận trạng thái an toàn hôm nay" | Button (primary, hành động riêng School Administration) | Ghi log sự kiện review | — | Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: Skeleton cho từng card.
- Empty: Không áp dụng (luôn có dữ liệu hôm nay, kể cả "chưa bắt đầu kiểm tra").
- Error: điểm checklist <60% → Alert cảnh báo (không phải banner cố định, MSG-093); AF1 — cảnh báo bước thất bại/quá hạn kèm link chi tiết non-conformance.
- Success: Ký xác nhận thành công, ghi log review.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Kiểm tra & Sự cố". Điều hướng ra: chi tiết non-conformance (nếu có cảnh báo AF1).
**Bố cục**: Dashboard Shell — Content: trạng thái 3-bước realtime (`col-span-8`) cạnh điểm vệ sinh hàng ngày (`col-span-4`), gallery ảnh xác minh + nút ký xác nhận cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Compliance Dashboard, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — trạng thái 3-bước kiểm tra realtime + card điểm vệ sinh hàng ngày, gallery ảnh xác minh, nút ký xác nhận cuối trang.
Thành phần bắt buộc: progress 3-bước theo lô, card điểm vệ sinh, alert cảnh báo bước thất bại/quá hạn,
alert cảnh báo điểm checklist <60% (không phải banner khóa), gallery ảnh, nút ký xác nhận.
Áp dụng Design System: alert dùng token danger/warning, progress dùng success/warning/danger theo từng bước.
Trạng thái cần thể hiện: error (bước thất bại/điểm thấp), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3469–3485 (Table 3-94 Daily Food Safety Monitoring Use Cases Specification)
- traceability_matrix.md §1.7 (UC-027 ↔ BR-100/BR-104)
- `business_rules.md` §12, ghi chú sau BR-208 (quyết định 05/07/2026 descope khóa bếp, không còn dùng BR-105/MSG-024 cho màn này).
