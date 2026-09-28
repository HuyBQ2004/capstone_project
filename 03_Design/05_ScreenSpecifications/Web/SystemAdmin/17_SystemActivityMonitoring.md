# Screen Spec — System Activity Monitoring

**Actor**: System Admin | **UC liên quan**: UC-057 (Report 3 §3.2 "Monitor System Activities") | **Status**: Draft
**BR liên quan**: BR-165 (báo cáo sức khỏe hệ thống hàng tuần), BR-170 (ghi nhật ký mọi hành động quan trọng)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chart CPU load | Chart (recharts, line) | Cập nhật gần thời gian thực | — | |
| 2 | Chart RAM load | Chart (recharts, line) | — | — | |
| 3 | Chart Disk I/O | Chart (recharts, line) | — | — | |
| 4 | Chỉ số Active user sessions | Card số liệu | — | — | |
| 5 | Chỉ số Database query pool load | Card số liệu / gauge | — | — | |
| 6 | Báo cáo tổng hợp hàng tuần | Card/Link "Xem báo cáo tuần" | — | BR-165 | |

### Trạng thái màn hình
- Loading: Skeleton cho từng chart/card.
- Empty: Không áp dụng.
- Error: EX1 — "Server monitoring API is unreachable" → hiển thị banner lỗi "Dịch vụ giám sát đang offline" thay cho chart.
- Success: Chart/card cập nhật số liệu mới nhất.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Giám sát & Audit". Không có màn hình con (có thể link sang báo cáo tuần dạng export).
**Bố cục**: Dashboard Shell — Content grid 12 cột: 3 chart CPU/RAM/Disk I/O xếp `col-span-4` mỗi chart hàng trên, 2 card chỉ số (Active sessions, DB pool) `col-span-6` hàng dưới, card báo cáo tuần cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình System Activity Monitoring, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — 3 chart line (CPU/RAM/Disk I/O) hàng trên, 2 card chỉ số (Active sessions/DB pool) hàng dưới, card link báo cáo tuần.
Thành phần bắt buộc: 3 chart, 2 card số liệu, card báo cáo tuần.
Áp dụng Design System: dùng recharts với màu theo token Design_System.md.
Trạng thái cần thể hiện: loading, error (dịch vụ giám sát offline), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4795–4815 (Table 3-145 Monitor System Activities Use Cases Specification)
- traceability_matrix.md §1.12 (UC-057 ↔ BR-165, BR-170)
