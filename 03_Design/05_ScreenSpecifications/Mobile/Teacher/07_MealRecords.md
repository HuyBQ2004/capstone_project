# Screen Spec — Meal Records

**Actor**: Teacher | **UC liên quan**: UC-056 - Monitor Student Meal History; UC-080 - Student Meal Consumption | **Status**: Draft
**BR liên quan**: BR-121 (theo dõi lịch sử bữa ăn), BR-125 (yêu cầu bữa ăn đặc biệt khi phục vụ), BR-133 (tóm tắt bữa ăn hàng ngày cho phụ huynh)
**MSG liên quan**: MSG-004, MSG-005, MSG-021

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Meal Records — [Tên học sinh]" hoặc danh sách cả lớp | UC-056 bước 1-3 | Có 2 chế độ: danh sách tóm tắt cả lớp / chi tiết 1 học sinh |
| 2 | Date Range Filter | Filter/DatePicker | Lọc theo khoảng ngày | UC-056 AF1 | |
| 3 | Meal History Summary List | Card list | Ngày, món ăn đã phục vụ, trạng thái tiêu thụ (Fully/Partially/Not Consumed) | BR-121 | |
| 4 | Consumption Record Input (chỉ với học sinh chế độ ăn đặc biệt) | Radio group | "Fully Consumed" / "Partially Consumed" / "Not Consumed" | BR-121, BR-125 | UC-080 bước 3 — chỉ hiển thị cho học sinh có special dietary plan |
| 5 | Save Consumption Button | Button (primary) | Lưu ghi nhận, validate đủ trường bắt buộc | BR-121 | MSG-021 nếu trùng lặp ghi nhận trong cùng phiên |
| 6 | Special Meal Compliance Badge | Badge | Đánh dấu tuân thủ chế độ ăn đặc biệt trong phiên | BR-125 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có dữ liệu bữa ăn trong khoảng thời gian đã chọn" — gợi ý đổi bộ lọc (UC-056 AF2 tương tự).
- Error: `SnackBar` `danger` + MSG-004/MSG-005; MSG-021 khi ghi nhận trùng lặp.
- Success: `SnackBar` `success` khi lưu ghi nhận tiêu thụ thành công.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Detail ("View Meal Records") hoặc Teacher Home. Không có trong sơ đồ mermaid gốc của `07_Teacher_Screen_Flow.md` — bổ sung theo UC-056/UC-080 [Giả định về vị trí điều hướng — cần team xác nhận].
**Bố cục**: AppBar (back) → Date Range Filter → danh sách `Card` lịch sử bữa ăn → (nếu học sinh đặc biệt) khối Consumption Record Input + Save button cố định dưới màn hình.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal Records, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Date Range Filter + danh sách Card lịch sử bữa ăn + khối ghi nhận tiêu thụ (radio group) + nút Save cố định dưới.
Thành phần bắt buộc: Date Range Filter, Meal History Summary List, Consumption Record Input, Save Consumption Button, Special Meal Compliance Badge.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005/021), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `traceability_matrix.md` §1.9 UC-056, UC-080.
- `business_rules.md` BR-121, BR-125, BR-133.
- Report 3.md, dòng 3887–3928 (UC-056 Monitor Student Meal History, UC-080 Student Meal Consumption — Normal Flow đầy đủ).
