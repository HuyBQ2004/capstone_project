# Screen Spec — Invitation Status

**Actor**: School Administration | **UC liên quan**: bundled trong UC-024 (Create Staff)/UC-010 (Bulk Import) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-010 (xác minh email trước đăng nhập), BR-011 (thời hạn token kích hoạt 48h)
**MSG liên quan**: MSG-004, MSG-005

> **Nguồn**: Không phải UC riêng — theo dõi trạng thái xác minh email (BR-010) của tài khoản vừa tạo qua `Upload Staff List`/`Bulk Import Student Profiles`. Nội dung suy luận từ postcondition chung của UC-024/UC-010/UC-154 (Activate Account & Set Password).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Status (Pending Verification/Active/Expired) | Select | — | BR-010, BR-011 | |
| 2 | Bảng danh sách lời mời | Table | Cột: Tên, Email, Ngày gửi, Trạng thái, Hạn token (48h) | BR-011 | |
| 3 | Badge trạng thái | Badge (warning=Pending, success=Active, danger=Expired) | — | — | |
| 4 | Nút "Gửi lại email xác minh" | Button (secondary) | Chỉ enable khi Expired/Pending quá lâu | BR-011 | Gia hạn thêm 48h theo AF1 UC-154 |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có lời mời nào đang chờ xác minh".
- Error: MSG-004/MSG-005.
- Success: Bảng hiển thị đầy đủ; gửi lại thành công → cập nhật trạng thái + hạn token mới.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Staff Management` (tab/link "Invitation Status") — cũng dùng chung logic cho `Bulk Import Student Profiles` (theo dõi tài khoản phụ huynh mới).
**Bố cục**: Dashboard Shell — filter bar (status), bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Invitation Status, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (status), bảng dữ liệu (Tên/Email/Ngày gửi/Trạng thái/Hạn token).
Thành phần bắt buộc: filter status, bảng, badge trạng thái, nút Gửi lại email xác minh.
Áp dụng Design System: badge warning/success/danger.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1271–1291 (UC-024), 1933–1953 (UC-010), 1579–1598 (UC-154 Activate Account & Set Password, AF1 resend)
- business_rules.md BR-010, BR-011
