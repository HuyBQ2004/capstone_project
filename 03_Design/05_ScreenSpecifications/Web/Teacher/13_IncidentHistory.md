# Screen Spec — Incident History

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident (xem lại báo cáo đã gửi) | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Page Header | Text | "Incident History" | — | |
| 2 | Assessment Filter Tabs | Tabs | All/Pending Assessment/Under Assessment | BR-147 | |
| 3 | Incident Log Table | `Table` | Học sinh, triệu chứng, mức độ, ngày báo cáo, badge trạng thái | BR-147, BR-155 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Bạn chưa gửi báo cáo sự cố nào".
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sự cố Sức khỏe" (theo `12_Teacher_Screen_Flow_Web.md`).
**Bố cục**: Topbar + Sidebar + Content: Page Header + Assessment Filter Tabs + `Table` phân trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident History, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Page Header + Filter Tabs + Table báo cáo sự cố.
Thành phần bắt buộc: Assessment Filter Tabs, Incident Log Table.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG thêm action chỉnh sửa/đóng sự cố.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6388–6402 (bản Mobile gốc).
- `03_Design/04_ScreenFlow/12_Teacher_Screen_Flow_Web.md`.
- `business_rules.md` BR-146, BR-147, BR-154, BR-155.
