# Screen Spec — System Status Screen

**Actor**: System Admin | **UC liên quan**: UC-149 (Report 3 §3.2 "View System Status") | **Status**: Draft
**BR liên quan**: BR-160 (sao lưu tự động), BR-165 (báo cáo sức khỏe hệ thống hàng tuần)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Health-check dashboard các dịch vụ lõi | Card list (mỗi service 1 card, badge success/danger) | Health-check thực hiện ngay khi vào trang (online) | — | VD: Database, Email Service, Firebase, Gemini AI, GPS Service |
| 2 | Panel trạng thái backup scheduler | Panel (xem `14_BackupManagement.md`) | — | BR-160 | Bundled trong UC-149 bước 4 |
| 3 | Nút "Export" (CSV/PDF) | Button | — | — | AF1 Export System Status Records |

### Trạng thái màn hình
- Loading: Skeleton cho health-check cards trong lúc chạy online health check.
- Empty: Không áp dụng.
- Error: Card service nào offline hiển thị badge danger + thời gian mất kết nối gần nhất.
- Success: Tất cả card hiển thị trạng thái mới nhất; Export tải file CSV/PDF (AF1).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Sidebar nhóm "Giám sát & Audit". Chứa panel con `Backup Management`.
**Bố cục**: Dashboard Shell — Page Header (nút Export góc phải), Content: card-grid health-check dịch vụ lõi (`col-span-3` mỗi card), panel Backup Management bên dưới chiếm toàn bộ chiều rộng.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình System Status Screen, nền tảng Web, actor System Admin.
Bố cục: Dashboard Shell — Page Header (nút Export), card-grid health-check dịch vụ lõi, panel Backup Management bên dưới.
Thành phần bắt buộc: card health-check từng service (badge success/danger), panel backup scheduler, nút Export.
Áp dụng Design System: badge success/danger cho từng service.
Trạng thái cần thể hiện: error (service offline), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4971–4991 (Table 3-153 View System Status Use Cases Specification)
- traceability_matrix.md §1.12 (UC-149 ↔ BR-165)
- 14_BackupManagement.md (panel con)
