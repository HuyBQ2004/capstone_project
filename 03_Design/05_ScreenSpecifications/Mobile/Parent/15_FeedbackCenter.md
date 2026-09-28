# Screen Spec — Feedback Center

**Actor**: Parent | **UC liên quan**: — (màn hub điều hướng, không gắn UC riêng — dẫn tới UC-085 và UC-073) | **Status**: Draft
**BR liên quan**: BR-139 (cửa sổ 24h nộp phản hồi), BR-146 (tự động kích hoạt điều tra sự cố)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có wireframe field-by-field riêng trong Report 3 cho màn hub này — suy ra từ `05_Parent_Screen_Flow.md` (FeedbackCenter → MealFeedback / FoodQualityFeedback / ReportSafety). Đóng vai trò điểm vào trung tâm cho 3 luồng phản hồi khác nhau.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title Header | Text | "Feedback Center" | — | |
| 2 | Recent Meal Feedback Tile | Button/Card | → Submit Meal Feedback (đánh giá món ăn cụ thể) | BR-139 | |
| 3 | Food Quality Feedback Tile | Button/Card | → Submit Food Quality Feedback (phản hồi chất lượng chung) | BR-139 | |
| 4 | Report Safety Concern Tile | Button/Card (đỏ, nổi bật) | → Report Food Safety Concern | BR-146 | |
| 5 | My Feedback History List | Card list | Danh sách phản hồi đã gửi trước đó + trạng thái phản hồi từ Service Caterers (nếu có) | BR-143 | [Giả định — cần xác nhận có hiển thị lịch sử tại đây hay không] |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có phản hồi nào được gửi".
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (màn hub).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Parent Dashboard ("Meal Feedback" tile). Ra: Submit Meal Feedback, Submit Food Quality Feedback, Report Food Safety Concern.
**Bố cục**: AppBar (back) → 3 Card/Tile lớn (Meal Feedback/Food Quality/Report Safety) → My Feedback History List cuộn dọc.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Feedback Center, nền tảng Mobile, actor Parent.
Bố cục: AppBar + 3 Tile lớn (Meal Feedback, Food Quality Feedback, Report Safety Concern — tile cuối màu đỏ nổi bật) + danh sách lịch sử phản hồi.
Thành phần bắt buộc: Recent Meal Feedback Tile, Food Quality Feedback Tile, Report Safety Concern Tile, My Feedback History List.
Áp dụng Design System: Report Safety Concern Tile màu danger.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/05_Parent_Screen_Flow.md` (FeedbackCenter → MealFeedback/FoodQualityFeedback/ReportSafety).
- `traceability_matrix.md` §1.10 UC-085, §1.11 UC-073.
- `business_rules.md` BR-139, BR-143, BR-146.
