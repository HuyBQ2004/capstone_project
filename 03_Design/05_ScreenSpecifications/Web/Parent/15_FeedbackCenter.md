# Screen Spec — Feedback Center

**Actor**: Parent | **UC liên quan**: — (màn hub, dẫn tới UC-085, UC-073) | **Status**: Draft
**BR liên quan**: BR-139, BR-146
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Feedback Center" | — | |
| 2 | Recent Meal Feedback Tile | Card/Button | → Submit Meal Feedback | BR-139 | |
| 3 | Food Quality Feedback Tile | Card/Button | → Submit Food Quality Feedback | BR-139 | |
| 4 | Report Safety Concern Tile | Card/Button (destructive) | → Report Food Safety Concern | BR-146 | |
| 5 | My Feedback History Table | `Table` | Lịch sử phản hồi đã gửi + trạng thái | BR-143 | [Giả định] |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có phản hồi nào được gửi".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Phản hồi & Sự cố" (`10_Parent_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: 3-card grid (Meal Feedback/Food Quality/Report Safety) + Table lịch sử phản hồi.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Feedback Center, nền tảng Web, actor Parent.
Bố cục: Topbar + Sidebar + 3-card grid (tile Report Safety màu destructive) + Table lịch sử phản hồi.
Thành phần bắt buộc: Recent Meal Feedback Tile, Food Quality Feedback Tile, Report Safety Concern Tile, My Feedback History Table.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `03_Design/04_ScreenFlow/10_Parent_Screen_Flow_Web.md`.
- `traceability_matrix.md` §1.10 UC-085, §1.11 UC-073.
- `business_rules.md` BR-139, BR-143, BR-146.
