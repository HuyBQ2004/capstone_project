# Screen Spec — School Administration Dashboard

**Actor**: School Administration | **UC liên quan**: entry point (không phải 1 UC riêng — tổng hợp shortcut) | **Status**: Draft
**BR liên quan**: BR-176 (phạm vi báo cáo giới hạn theo trường), BR-180 (dashboard cập nhật trong 5 phút nếu tái sử dụng widget Analytics)
**MSG liên quan**: MSG-004, MSG-005

> Node gốc của sơ đồ điều hướng School Administration (`02_School_Administration_Screen_Flow.md`). Số liệu tóm tắt suy ra từ các UC nguồn liên quan (UC-114 Incident count, UC-131/127 Partnership pending count, UC-128 Menu pending, UC-027 Compliance hôm nay) — không bịa thêm chỉ số ngoài các UC này.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card số sự cố đang mở (Active/Under Investigation) | Card + số liệu | Badge danger nếu > 0 | — | Nguồn: UC-114 View Food Poisoning Incident List |
| 2 | Card số hồ sơ đối tác đang chờ duyệt (Supplier + Caterer) | Card + số liệu | — | — | Nguồn: UC-131, UC-127 (lọc Pending) |
| 3 | Card số thực đơn chờ duyệt | Card + số liệu | — | — | Nguồn: UC-128 View List Menu (lọc Pending Approval) |
| 4 | Card trạng thái tuân thủ hôm nay (3-bước kiểm tra) | Card + badge | success/warning/danger | BR-091 | Nguồn: UC-027 Daily Food Safety Monitoring |
| 5 | Danh sách shortcut điều hướng theo module | Card link/menu tile | — | — | Ánh xạ tới các nhóm: Đối tác, Thực đơn, Lớp/Học sinh, Sự cố, Thông báo, Nhân viên, Báo cáo |
| 6 | Cảnh báo gần nhất (vi phạm/override abuse) | Alert list rút gọn | — | BR-189, BR-190 | Nguồn: UC-181 Violation Registry, UC-183 |

### Trạng thái màn hình
- Loading: Skeleton từng card, tải song song.
- Empty: Không áp dụng (dashboard luôn có số liệu, kể cả 0).
- Error: Card lỗi tải hiển thị trạng thái lỗi cục bộ, không chặn card khác.
- Success: Card hiển thị số liệu mới nhất trong phạm vi trường (BR-176).

---

## Platform Layout — Web
**Entry/Exit points**: Trang landing sau khi School Administration đăng nhập thành công. Điều hướng tới toàn bộ các nhóm màn hình còn lại qua Sidebar hoặc card shortcut.
**Bố cục**: Dashboard Shell — Content grid 12 cột: 4 card số liệu hàng trên (`col-span-3`), danh sách shortcut dạng card-grid theo module ở giữa trang, alert cảnh báo gần nhất cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Administration Dashboard, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Sidebar trái + Topbar + Content dạng grid (card số liệu hàng trên, shortcut tile giữa trang, alert cuối trang).
Thành phần bắt buộc: card sự cố đang mở, card đối tác chờ duyệt, card thực đơn chờ duyệt, card trạng thái tuân thủ hôm nay, lưới shortcut theo module, alert cảnh báo vi phạm gần nhất.
Áp dụng Design System: badge success/warning/danger theo trạng thái.
Trạng thái cần thể hiện: loading, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- 03_Design/04_ScreenFlow/02_School_Administration_Screen_Flow.md (node gốc "Dash")
- Report 3.md (UC-114, UC-131, UC-127, UC-128, UC-027 — nguồn số liệu tóm tắt)
- traceability_matrix.md (BR-176, BR-180, BR-189, BR-190)
