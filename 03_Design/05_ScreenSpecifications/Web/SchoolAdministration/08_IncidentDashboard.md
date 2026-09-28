# Screen Spec — Incident Dashboard

**Actor**: School Administration | **UC liên quan**: entry point tổng hợp (không phải 1 UC riêng — điểm vào của UC-114/116/121/005/013/031) | **Status**: Draft
**BR liên quan**: BR-146 (tự động kích hoạt điều tra), BR-148 (cảnh báo ngay cho sự cố nghiêm trọng)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Card tổng số sự cố đang mở theo severity | Card + badge (danger=High, warning=Medium, info=Low) | — | BR-147 | |
| 2 | Card số CAPA đang chờ duyệt | Card + số liệu | — | BR-152 | Nguồn: UC-005 |
| 3 | Danh sách sự cố gần nhất (rút gọn) | Table rút gọn | — | — | Link sang `Incident List` |
| 4 | Nút "Xem toàn bộ danh sách sự cố" | Button/Link | — | — | Mở `Incident List` |
| 5 | Nút "Export Incident Reports" | Button | — | BR-154 | Mở `Export Incident Reports` (UC-031) |

### Trạng thái màn hình
- Loading: Skeleton cho từng card.
- Empty: "Không có sự cố nào đang mở" (trạng thái tích cực).
- Error: MSG-004/MSG-005.
- Success: Card + bảng rút gọn hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Kiểm tra & Sự cố". Điều hướng ra: `Incident List`, `Export Incident Reports`.
**Bố cục**: Dashboard Shell — card số liệu hàng trên (`col-span-4` mỗi card), bảng rút gọn sự cố gần nhất bên dưới, nút Export góc phải Page Header.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident Dashboard, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — card số liệu (sự cố theo severity, CAPA chờ duyệt) hàng trên, bảng rút gọn sự cố gần nhất bên dưới.
Thành phần bắt buộc: card severity, card CAPA chờ duyệt, bảng rút gọn, nút Xem toàn bộ, nút Export.
Áp dụng Design System: badge danger/warning/info theo severity.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- 03_Design/04_ScreenFlow/02_School_Administration_Screen_Flow.md (node "ID" Incident Dashboard, entry point tổng hợp)
- business_rules.md BR-146, BR-147, BR-148, BR-152
