# Screen Spec — Usage Statistics Screen

**Actor**: System Admin | **UC liên quan**: UC-150 (Report 3 §3.2 "View Usage Statistics") | **Status**: Draft
**BR liên quan**: BR-165 (báo cáo sức khỏe hệ thống và số liệu người dùng hàng tuần)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Date range | DatePicker (range) | — | — | |
| 2 | Filter theo Role | Select | — | — | |
| 3 | Chart Active users theo ngày/tuần | Chart (recharts, bar/line) | — | BR-165 | |
| 4 | Bảng Top use case được gọi nhiều nhất | Table rút gọn | — | BR-165 | |
| 5 | Chart Dung lượng lưu trữ sử dụng | Chart (recharts, gauge/donut) | — | — | |
| 6 | Chart API request count | Chart (recharts, line) | — | BR-165 | |

### Trạng thái màn hình
- Loading: Skeleton cho từng chart/bảng.
- Empty: Không áp dụng (luôn có ít nhất số liệu 0 trong khoảng thời gian chọn).
- Error: MSG-004/MSG-005.
- Success: Chart/bảng cập nhật theo filter Date range/Role.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Giám sát & Audit". Không có màn hình con.
**Bố cục**: Dashboard Shell — filter bar (Date range, Role) trên cùng, Content grid 12 cột: chart Active users `col-span-8`, bảng Top use case `col-span-4` cạnh nhau, 2 chart Dung lượng/API request `col-span-6` mỗi chart hàng dưới.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Usage Statistics Screen, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — filter bar (Date range, Role), chart Active users + bảng Top use case hàng trên, 2 chart (Dung lượng, API request) hàng dưới.
Thành phần bắt buộc: filter Date/Role, chart Active users, bảng Top use case, chart dung lượng lưu trữ, chart API request count.
Áp dụng Design System: dùng recharts với token màu Design_System.md.
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4993–5013 (Table 3-154 View Usage Statistics Use Cases Specification)
- traceability_matrix.md §1.12 (UC-150 ↔ BR-165)
