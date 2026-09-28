# Screen Spec — System Usage Metrics

**Actor**: School Administration | **UC liên quan**: UC-171 - View Analytics Dashboard (Report 3.md §3.2.13.1, lọc phạm vi 1 trường — School Administration được liệt kê là Primary Actor của UC-171) | **Status**: Draft
**BR liên quan**: BR-175 (phân quyền báo cáo), BR-176 (phạm vi theo trường), BR-180 (cập nhật trong 5 phút)
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: UC-171 nay đã có Functional Specification đầy đủ trong Report 3.md. Khác với `20_UsageStatisticsScreen.md` của System Admin (UC-150 View Usage Statistics riêng, phạm vi toàn hệ thống, đa trường), màn hình này dùng UC-171 với phạm vi **thu hẹp trong 1 trường** (BR-176) — 2 UC khác nhau phục vụ 2 actor khác nhau, không trùng lặp.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | — | **[Giả định]** |
| 2 | Chart Active users (staff/parent) theo ngày | Chart (recharts) | Chỉ trong phạm vi trường | BR-176 | **[Giả định]** |
| 3 | Bảng Top tính năng được dùng nhiều nhất | Table rút gọn | — | — | **[Giả định]** |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]**.
- Error: MSG-004/MSG-005.
- Success: **[Giả định]** — cập nhật trong 5 phút nếu áp dụng BR-180.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê".
**Bố cục**: Dashboard Shell — filter bar + chart + bảng top tính năng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình System Usage Metrics, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range, chart Active users theo ngày, bảng Top tính năng dùng nhiều nhất, nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.13.1 (UC-171 View Analytics Dashboard) — bổ sung 2026-07-01
- traceability_matrix.md §1.13
- business_rules.md BR-175, BR-176, BR-180
- 14_Screen_Authorization_Matrix.md khối 3
