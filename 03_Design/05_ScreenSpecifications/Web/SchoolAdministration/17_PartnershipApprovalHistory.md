# Screen Spec — Partnership Approval History

**Actor**: School Administration | **UC liên quan**: UC-106 - View Audit Logs (Report 3.md dòng 4883–4903, lọc theo Resource = Partnership/Supplier/Caterer) | **Status**: Draft
**BR liên quan**: BR-170 (ghi log mọi hành động quan trọng — bao gồm approve/reject)
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: màn hình này KHÔNG cần UC mới. `14_Screen_Authorization_Matrix.md` khối "Audit Logs" xác nhận School Administration đã có quyền xem Audit Logs (`x` ở cả cột System Admin và School Admin). UC-106 (View Audit Logs) đã có Functional Specification đầy đủ, hiển thị đúng các cột cần thiết (Timestamp, User ID, Action Performed, Resource Accessed, Status) — Partnership Approval History là 1 view lọc theo Resource Accessed = "Partnership/Supplier/Caterer" trong màn hình Audit Logs dùng chung, không phải màn hình/UC riêng biệt.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | — | **[Giả định]** |
| 2 | Filter loại đối tác (Supplier/Service Caterers) | Select | — | — | **[Giả định]** |
| 3 | Filter kết quả (Approved/Rejected) | Select | — | — | **[Giả định]** |
| 4 | Bảng lịch sử duyệt | Table (đọc-only) | Cột: Tên đối tác, Loại, Kết quả, Người duyệt, Ngày, Lý do (nếu Reject) | BR-170 | **[Giả định — cấu trúc cột dựa trên Normal Flow bước 7-9 của UC-004/UC-007]** |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Chưa có lịch sử duyệt nào khớp bộ lọc".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Quản lý Nhà cung cấp" (cạnh Catering/Supplier Partnership Requests).
**Bố cục**: Dashboard Shell — filter bar + bảng lịch sử đọc-only.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Partnership Approval History, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range + loại đối tác + kết quả (Approved/Rejected), bảng lịch sử duyệt đọc-only (Tên đối tác, Loại, Kết quả, Người duyệt, Ngày, Lý do nếu Reject).
Đây là 1 view lọc trong màn hình Audit Logs dùng chung (UC-106), không phải màn hình độc lập — có thể tái dùng layout Audit Logs nếu team quyết định không tách trang riêng.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4883–4903 (UC-106 View Audit Logs) — xác nhận 2026-07-01
- 14_Screen_Authorization_Matrix.md khối "Audit Logs" (School Admin = x)
- Report 3.md, dòng 2269–2307 (Normal Flow UC-004/UC-007, bước ghi audit trail — nguồn gốc dữ liệu approve/reject)
- business_rules.md BR-170
