# Screen Spec — Meal History

**Actor**: Parent | **UC liên quan**: UC-133 - View Meal History | **Status**: Draft
**BR liên quan**: BR-031, BR-069, BR-070, BR-133 (tóm tắt bữa ăn hàng ngày)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Meal History Log" | — | Nguồn: Report 3 §3.3.2.4 |
| 2 | Instruction Text | Text | "Select calendar day to see child's historical consumption" | — | |
| 3 | Calendar Strip Component | Interactive calendar (2 tuần) | Ngày disabled/selectable/active theo lịch sử có dữ liệu | BR-133 | |
| 4 | Meal History Summary Card | Card | Ngày, tỷ lệ tiêu thụ (%), loại khẩu phần, trạng thái hài lòng | BR-121, BR-133 | Tap → Meal History Detail |
| 5 | Weekly Charts Button | Button | → Nutrition History | — | |
| 6 | Special Meals History Button | Button | → Special Meal Records | — | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Không có lịch sử bữa ăn trong khoảng thời gian đã chọn" (UC-133 AF2).
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Meal History" tile). Ra: Meal History Detail (chọn ngày), Nutrition History, Special Meal Records.
**Bố cục**: AppBar (back) → Instruction Text → Calendar Strip Component → Meal History Summary Card → 2 nút điều hướng (Weekly Charts/Special Meals History).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Meal History, nền tảng Mobile, actor Parent.
Bố cục: AppBar + Instruction Text + Calendar Strip 2 tuần + Meal History Summary Card + 2 nút điều hướng (Weekly Charts/Special Meals History).
Thành phần bắt buộc: Instruction Text, Calendar Strip Component, Meal History Summary Card, Weekly Charts Button, Special Meals History Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6161–6178 (Meal History Log, field-by-field gốc), dòng 3975–3995 (UC-133 Normal/Alternative Flow).
- `business_rules.md` BR-031, BR-069, BR-070, BR-121, BR-133.
