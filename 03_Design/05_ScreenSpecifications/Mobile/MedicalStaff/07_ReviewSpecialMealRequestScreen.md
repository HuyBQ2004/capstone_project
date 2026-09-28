# Screen Spec — Review Special Meal Request Screen

**Actor**: Medical Staff | **UC liên quan**: UC-078 (Report 3 §3.2, dòng 2065–2083) | **Status**: Draft
**BR liên quan**: BR-031 (xét duyệt), BR-066 (danh mục chế độ ăn đặc biệt: Halal/Vegetarian/Gluten-Free/Nut-Free)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Name & Class (context) | Text | — | — | Kế thừa từ `08_SpecialMealRequestList.md` |
| 2 | Dietary type | Text/Select (đọc) | Phải thuộc danh mục chuẩn (BR-066) | BR-066 | UC-078 bước 3–4 |
| 3 | Duration | Text (đọc) | — | — | |
| 4 | Medical justification | Textarea (đọc) | — | — | |
| 5 | Tài liệu minh chứng (nếu có) | Ảnh/File preview | — | BR-031 | |
| 6 | Nút "Approve" | Button (primary) | Chỉ bật khi dietary type khớp danh mục chuẩn | BR-066 | Điều hướng `09_ApproveSpecialMealDialog.md` |
| 7 | Nút "Reject" | Button (destructive) | — | — | Điều hướng `10_RejectSpecialMealDialog.md` |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` khi tải chi tiết yêu cầu.
- Empty: không áp dụng (luôn có 1 yêu cầu cụ thể khi vào màn này).
- Error: MSG-004, MSG-005; dietary type không thuộc danh mục chuẩn (BR-066) → cảnh báo, chặn Approve cho tới khi Medical Staff xác nhận phân loại lại.
- Success: chuyển tiếp sang dialog Approve/Reject tương ứng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ chọn 1 Special Meal Card trên `08_SpecialMealRequestList.md`; Approve/Reject → dialog tương ứng, sau đó quay lại List.
**Bố cục**: trang riêng full-screen, AppBar Back/Home, block thông tin xếp dọc (Dietary type → Duration → Medical justification → tài liệu), 2 nút Approve/Reject cố định cuối màn.

## Platform Layout — Web
**Entry/Exit points**: Vào từ chọn dòng trên `08_SpecialMealRequestList.md` (Web); Approve/Reject → `Dialog` tương ứng.
**Bố cục**: `Sheet` bên phải chứa toàn bộ thông tin yêu cầu + tài liệu minh chứng, 2 nút Approve/Reject cố định cuối Sheet.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Review Special Meal Request, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Dietary type, Duration, Medical justification, tài liệu minh chứng, nút Approve, nút Reject.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, error (dietary type không hợp lệ).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2065–2083 (UC-078).
- business_rules.md (BR-031, BR-066).
- error_message.md (MSG-004, MSG-005).
