# Screen Spec — Approve/Reject Violation Resolution

**Actor**: School Administration | **UC liên quan**: UC-182 (Report 3 §3.2 "Approve/Reject Violation Resolution") | **Status**: Draft
**BR liên quan**: BR-186 (phân cấp phê duyệt — Medium/High cần School Admin), BR-211 (xem ghi chú)
**MSG liên quan**: MSG-004, MSG-005, MSG-089 (phê duyệt thành công, áp dụng kỷ luật)

> **Ghi chú BR-211 (06/07/2026)**: hồ sơ chỉ tới được màn hình này (Pending Approval) sau khi đã vượt qua gate bắt buộc "phải có ảnh biên bản đã ký 3 bên" ở `Web/MedicalStaff/24_RecordViolationResolution.md` — do đó `signedRecordPhotoUrl` **luôn đã tồn tại** khi School Administration xem/duyệt tại đây. Không cần thêm validation/gate riêng ở màn này.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Chi tiết vi phạm gốc | Text hiển thị | — | — | |
| 2 | Đề xuất xử lý + mức kỷ luật | Text hiển thị | — | BR-185 | |
| 3 | Nút "Approve" | Button (primary) | — | BR-186 | Đóng vi phạm gốc, áp dụng kỷ luật, ghi log |
| 4 | Nút "Reject" + lý do bắt buộc | Button (destructive) + Textarea | Bắt buộc lý do (AF1) | BR-186 | Trả về Medical Staff để sửa lại |

### Trạng thái màn hình
- Loading: Button Approve/Reject loading.
- Empty: Không áp dụng.
- Error: AF1 — thiếu lý do khi Reject → validation error, chặn submit; MSG-004/MSG-005.
- Success: MSG-089 khi Approve (đóng vi phạm gốc, áp dụng kỷ luật); toast xác nhận khi Reject (trả về Medical Staff, thông báo).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Violation Registry` (chọn bản ghi Pending Approval). Sau khi xử lý quay về `Violation Registry`.
**Bố cục**: Dashboard Shell — Page Header (nút Approve/Reject góc phải), Content: chi tiết vi phạm + đề xuất xử lý (1 cột, `col-span-8` căn giữa).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Approve/Reject Violation Resolution, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject), nội dung chi tiết vi phạm + đề xuất xử lý.
Thành phần bắt buộc: chi tiết vi phạm gốc, đề xuất xử lý + mức kỷ luật, nút Approve, nút Reject + textarea lý do bắt buộc.
Áp dụng Design System: nút Reject dùng token danger.
Trạng thái cần thể hiện: error (thiếu lý do reject), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4705–4725 (Table 3-163 Approve/Reject Violation Resolution Use Cases Specification)
- traceability_matrix.md §1.11 (UC-182 ↔ BR-186)
