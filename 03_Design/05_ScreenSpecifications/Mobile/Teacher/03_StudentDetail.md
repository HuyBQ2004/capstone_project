# Screen Spec — Student Detail

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info (một phần), UC-056 (bước 4-5, chi tiết học sinh) | **Status**: Draft
**BR liên quan**: BR-030 (liên kết học sinh–phụ huynh), BR-123 (cảnh báo dị ứng)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: Report 3 §3.3.2.6 (Teacher Screens) không có field-by-field riêng cho "Student Detail" — chỉ có link "Profile" từ Student List. Field dưới đây suy ra từ UC-145 (Normal Flow xem thông tin dị ứng/sức khỏe học sinh) + nhu cầu điều hướng tới 3 màn con (Allergy Information, Health Information, Special Meal Requirements, Meal Records) theo `07_Teacher_Screen_Flow.md`. Đánh dấu rõ các field không có nguồn UC/BR tường minh.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Summary Card | Card | Avatar, họ tên, lớp, ngày sinh | BR-030 | [Giả định — cần team xác nhận về field ngày sinh cụ thể] |
| 2 | Allergy Status Badge | Badge | "Có dị ứng" (đỏ)/"Không dị ứng" (xám) | BR-030, BR-123 | Điều hướng Allergy Information |
| 3 | Special Diet Badge | Badge | "Chế độ ăn đặc biệt" (vàng)/ẩn nếu không có | BR-065 | Điều hướng Special Meal Requirements |
| 4 | View Health Information Link | Nút/Link | Điều hướng Health Information | BR-030 | |
| 5 | View Meal Records Link | Nút/Link | Điều hướng Meal Records | BR-121 | |
| 6 | Parent Contact Info | Text | Tên phụ huynh liên kết (chỉ đọc) | BR-030 | [Giả định — cần team xác nhận mức độ chi tiết được phép hiển thị cho Teacher] |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: không áp dụng (luôn có dữ liệu học sinh khi vào từ danh sách).
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student List (tap "Profile"). Ra: Allergy Information, Health Information, Special Meal Requirements, Meal Records.
**Bố cục**: AppBar (back + tên học sinh) → Student Summary Card → các Badge trạng thái → danh sách link điều hướng dạng `Card` action.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Detail, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Student Summary Card + Badge trạng thái (dị ứng/chế độ ăn đặc biệt) + danh sách link điều hướng (Health Information/Allergy Information/Special Meal Requirements/Meal Records).
Thành phần bắt buộc: Student Summary Card, Allergy Status Badge, Special Diet Badge, View Health Information Link, View Meal Records Link.
Áp dụng Design System: badge danger/warning theo Design_System.md mục 3.
Trạng thái cần thể hiện: loading, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md` (suy luận điều hướng — không có wireframe gốc Report 3 cho màn này).
- `traceability_matrix.md` §1.3 UC-145, §1.9 UC-056.
- `business_rules.md` BR-030, BR-065, BR-123.
