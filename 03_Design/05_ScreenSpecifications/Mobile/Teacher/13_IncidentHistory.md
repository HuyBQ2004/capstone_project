# Screen Spec — Incident History

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident (xem lại báo cáo đã gửi) | **Status**: Draft
**BR liên quan**: BR-146, BR-154 (báo cáo sự cố chính thức), BR-155 (đóng sự cố — chỉ School Administration, Teacher chỉ xem)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: không có UC "View Incident History" riêng cho Teacher trong `traceability_matrix.md` — màn hình chỉ hiển thị lại dữ liệu do UC-074 tạo ra, ở chế độ chỉ đọc, giới hạn phạm vi báo cáo của chính giáo viên đó (theo `07_Teacher_Screen_Flow.md`: "chỉ hiển thị các báo cáo do giáo viên đó tạo, không có quyền xem toàn bộ danh sách incident").

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Home Shortcut Button | Button | Về Teacher Home | — | Nguồn: Report 3 §3.3.2.6 |
| 2 | Screen Title Header | Text | "Incident History" | — | |
| 3 | Assessment Filter Tabs | Tabs | "All" / "Pending Assessment" / "Under Assessment" | BR-147 | |
| 4 | Incident Log Card | Card list (1 báo cáo/card) | Học sinh liên quan, triệu chứng, badge mức độ (Mild/Medium), ngày báo cáo, badge trạng thái (Pending Assessment vàng/Concluded xanh) | BR-147, BR-155 | Chỉ đọc — trạng thái do Medical Staff/School Administration cập nhật |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Bạn chưa gửi báo cáo sự cố nào".
- Error: `SnackBar` `danger` + MSG-004/005.
- Success: không áp dụng (chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home ("View History") hoặc tab "Incidents". Không có màn con (chỉ đọc danh sách).
**Bố cục**: AppBar (Home shortcut + tiêu đề) → Assessment Filter Tabs → danh sách `Card` cuộn dọc → `BottomNavigationBar`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident History, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Assessment Filter Tabs (All/Pending Assessment/Under Assessment) + danh sách Card báo cáo sự cố + BottomNavigationBar.
Thành phần bắt buộc: Assessment Filter Tabs, Incident Log Card (học sinh, triệu chứng, badge mức độ, ngày, badge trạng thái).
Áp dụng Design System: Pending Assessment = warning, Concluded = success.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên. KHÔNG thêm action chỉnh sửa/đóng sự cố (ngoài quyền Teacher).
```

## Nguồn dữ liệu
- Report 3.md, dòng 6388–6402 (Incident History, field-by-field gốc).
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md`.
- `business_rules.md` BR-146, BR-147, BR-154, BR-155.
