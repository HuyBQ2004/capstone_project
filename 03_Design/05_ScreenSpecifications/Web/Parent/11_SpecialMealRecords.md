# Screen Spec — Special Meal Records

**Actor**: Parent | **UC liên quan**: UC-142 - View Special Meal Records | **Status**: Draft
**BR liên quan**: BR-065, BR-121, BR-123
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Special Meal Records" | — | |
| 2 | Special Meal Record Table | `Table` | Ngày, món gốc, món thay thế, allergen, badge xác nhận kép | BR-065, BR-123 | |
| 3 | Record Detail Dialog | `Dialog` (khi click hàng) | Ghi chú chuẩn bị, timestamp, trạng thái tiêu thụ | BR-121, BR-123 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có lịch sử bữa ăn đặc biệt".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Bữa ăn của con" (`10_Parent_Screen_Flow_Web.md`), từ Meal History.
**Bố cục**: Topbar + Sidebar + Content: Page Header + `Table` phân trang, click hàng mở `Dialog` chi tiết.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Records, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + Table phiên bữa ăn đặc biệt + Dialog chi tiết khi click hàng.
Thành phần bắt buộc: Special Meal Record Table, Record Detail Dialog.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4019–4039 (UC-142).
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `business_rules.md` BR-065, BR-121, BR-123.
