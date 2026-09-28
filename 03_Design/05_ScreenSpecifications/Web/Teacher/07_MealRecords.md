# Screen Spec — Meal Records

**Actor**: Teacher | **UC liên quan**: UC-056 - Monitor Student Meal History; UC-080 - Student Meal Consumption | **Status**: Draft
**BR liên quan**: BR-121, BR-125, BR-133
**MSG liên quan**: MSG-004, MSG-005, MSG-021

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Meal Records" + filter chọn học sinh/cả lớp | UC-056 | |
| 2 | Date Range Filter | DatePicker | Lọc khoảng ngày | UC-056 AF1 | |
| 3 | Meal History Table | `Table` | Ngày, món ăn, trạng thái tiêu thụ | BR-121 | |
| 4 | Consumption Record Input | Radio group (trong Dialog/Sheet) | Fully/Partially/Not Consumed | BR-121, BR-125 | Chỉ học sinh chế độ ăn đặc biệt |
| 5 | Save Consumption Button | Button (primary) | Validate, MSG-021 nếu trùng | BR-121 | |
| 6 | Special Meal Compliance Badge | Badge | Tuân thủ chế độ ăn đặc biệt | BR-125 | |

### Trạng thái màn hình
- Loading: `Skeleton` bảng.
- Empty: "Chưa có dữ liệu bữa ăn trong khoảng thời gian đã chọn".
- Error: `Alert variant="destructive"` + MSG-004/005; MSG-021 khi trùng lặp.
- Success: `Toast` `success`.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phân phát Bữa ăn" hoặc từ Student Detail.
**Bố cục**: Topbar + Sidebar + Content: Page Header + Date Range Filter + `Table` lịch sử bữa ăn; ghi nhận tiêu thụ mở qua `Dialog` ngắn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal Records, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + Date Range Filter + Table lịch sử bữa ăn + Dialog ghi nhận tiêu thụ.
Thành phần bắt buộc: Meal History Table, Consumption Record Input, Save Consumption Button, Special Meal Compliance Badge.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005/021), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `traceability_matrix.md` §1.9 UC-056, UC-080.
- `business_rules.md` BR-121, BR-125, BR-133.
- Report 3.md, dòng 3887–3928.
