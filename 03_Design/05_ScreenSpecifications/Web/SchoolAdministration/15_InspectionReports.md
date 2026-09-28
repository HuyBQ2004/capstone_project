# Screen Spec — Inspection Reports

**Actor**: School Administration | **UC liên quan**: UC-113 (Report 3 §3.2 "View Food Inspection Reports") | **Status**: Draft
**BR liên quan**: BR-091 (nhiệt độ giữ ấm), BR-097 (tích hợp quản lý sự cố), BR-098 (phê duyệt bữa ăn để phục vụ)
**MSG liên quan**: MSG-004, MSG-005, MSG-016, MSG-017

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | — | |
| 2 | Filter Meal session | Select | — | — | |
| 3 | Filter Service Caterers | Select | — | — | |
| 4 | Nút "Generate Report" | Button | — | — | |
| 5 | Bảng tổng hợp (tổng lô kiểm tra, tỷ lệ pass/fail, số lần lệch nhiệt độ nghiêm trọng, trạng thái CAPA) | Table | — | BR-091, BR-097, BR-098 | |
| 6 | Nút "Export Report" (PDF/Excel) | Button | — | — | AF1 UC-113 |

### Trạng thái màn hình
- Loading: Button "Generate Report" loading.
- Empty: "Không có dữ liệu kiểm tra trong khoảng thời gian đã chọn".
- Error: MSG-016 (checklist chưa đầy đủ), MSG-017 (nhiệt độ thấp) — hiển thị dưới dạng cảnh báo trong báo cáo tổng hợp; MSG-004/MSG-005.
- Success: Bảng tổng hợp hiển thị, Export tải file (AF1).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Kiểm tra & Sự cố" hoặc "Báo cáo & Thống kê". Không có màn hình con.
**Bố cục**: Dashboard Shell — filter bar + nút Generate Report, bảng tổng hợp giữa trang, nút Export cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspection Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Date/Meal session/Service Caterers), nút Generate Report, bảng tổng hợp, nút Export.
Thành phần bắt buộc: 3 filter, nút Generate Report, bảng tổng hợp (tổng lô/tỷ lệ pass-fail/lệch nhiệt độ/CAPA), nút Export.
Áp dụng Design System: không cần badge đặc biệt ngoài màu neutral cho bảng.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 3579–3595 (Table 3-99 View Food Inspection Reports Use Cases Specification)
- traceability_matrix.md §1.7 (UC-113 ↔ BR-099)
