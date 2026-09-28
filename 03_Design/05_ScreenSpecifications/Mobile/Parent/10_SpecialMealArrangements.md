# Screen Spec — Special Meal Arrangements

**Actor**: Parent | **UC liên quan**: UC-141 - View Special Meal Arrangements | **Status**: Draft
**BR liên quan**: BR-031, BR-065 (kế hoạch bữa ăn đặc biệt bắt buộc), BR-123 (cảnh báo dị ứng — xác nhận kép Kitchen/Teacher)
**MSG liên quan**: MSG-004, MSG-005, MSG-051

> **Ghi chú hợp nhất**: theo `05_Parent_Screen_Flow.md`, màn "Allergy Conflict Alert" (Report 3 dòng 6088–6103, trigger từ Dashboard banner/Weekly Menu card đỏ) là **cùng 1 đích điều hướng** với Special Meal Arrangements (UC-141) — field dưới đây hợp nhất cả 2 nguồn: cấu trúc tổng quan theo UC-141 Normal Flow + nội dung cảnh báo cụ thể theo wireframe "Allergy Conflict Alert" gốc.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Special Meal Arrangements" (hoặc "Allergen Conflict!" chữ đỏ nếu vào từ cảnh báo) | — | Nguồn: Report 3 dòng 6097 |
| 2 | Allergen Warning Card | Alert (danger) | Mô tả xung đột cụ thể giữa hồ sơ dị ứng và món ăn | BR-123 | MSG-051 |
| 3 | Special Meal Detail List | Card list | Loại allergen, mức độ, món thay thế được phân công, ghi chú accommodation | BR-065 | UC-141 bước 3 |
| 4 | Dual-Confirmation Status | Badge x2 | "Kitchen Released" / "Teacher Hand-off" — mỗi trạng thái riêng | BR-123 | UC-141 bước 5 |
| 5 | Parental Advisory Note | Text (italic) | "Phụ huynh nên nhắc con kiểm tra nhãn đỏ trên khay trước khi ăn" | — | Nguồn: Report 3 dòng 6100 |
| 6 | Submit Allergy Information Link | Link (chỉ hiện nếu chưa có hồ sơ dị ứng) | → Submit Allergy Information | — | UC-141 AF1 |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Học sinh chưa có kế hoạch bữa ăn đặc biệt nào" + link khai báo dị ứng (UC-141 AF1).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard (Allergy Threat Alert Box) hoặc Weekly Menu (Allergy Alert Meal Card). Ra: Submit Allergy Information (nếu Empty).
**Bố cục**: AppBar (back, tiêu đề đỏ nếu là cảnh báo) → Allergen Warning Card → Special Meal Detail List → Dual-Confirmation Status → Parental Advisory Note.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Special Meal Arrangements, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Allergen Warning Card (đỏ) + danh sách Card chi tiết bữa ăn đặc biệt + 2 Badge xác nhận kép (Kitchen/Teacher) + ghi chú khuyến cáo phụ huynh.
Thành phần bắt buộc: Allergen Warning Card, Special Meal Detail List, Dual-Confirmation Status, Parental Advisory Note.
Áp dụng Design System: Allergen Warning = danger bắt buộc (BR-123, BR-123).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005), warning (MSG-051).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4001–4017 (UC-141 Normal/Alternative Flow), dòng 6088–6103 (Allergy Conflict Alert, field-by-field gốc — hợp nhất theo `05_Parent_Screen_Flow.md`).
- `business_rules.md` BR-031, BR-065, BR-123; `error_message.md` MSG-051.
