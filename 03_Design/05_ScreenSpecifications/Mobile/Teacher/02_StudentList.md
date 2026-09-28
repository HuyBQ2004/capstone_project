# Screen Spec — Student List

**Actor**: Teacher | **UC liên quan**: UC-056 - Monitor Student Meal History (bước 1–2, class registry), tham chiếu thêm UC-074 (chọn học sinh liên quan khi báo cáo sự cố) | **Status**: Draft
**BR liên quan**: BR-025 (sức chứa lớp, roster), BR-030 (liên kết học sinh–phụ huynh, dữ liệu dị ứng/chế độ ăn)
**MSG liên quan**: MSG-004, MSG-005

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Home Shortcut Button | Button | Về Teacher Home | — | Nguồn: Report 3 §3.3.2.6 Student List in Class |
| 2 | Screen Title Header | Text | "Class [Tên lớp] Student List" | BR-025 | |
| 3 | Profile Filter Tabs | Tabs | "All" / "Allergy Warning" / "Special Diet" / "Normal" | BR-030, BR-065 | |
| 4 | Student Record Card | Card list (1 bản ghi/card) | Avatar initials, họ tên, badge tình trạng (Allergy Warning đỏ / Special Diet vàng / Normal xám), link "Profile" | BR-030 | Tap → Student Detail |
| 5 | Bottom Tab Navigation | Nav | 4 tab: Home, Students (active), Meals, Incidents | — | |

### Trạng thái màn hình
- Loading: `Shimmer` cho danh sách card.
- Empty: "Lớp chưa có học sinh nào" + hướng dẫn liên hệ School Administration [Giả định].
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng (màn hình chỉ đọc).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Teacher Home (tab "Students"), từ Meal Distribution ("select class"), từ Incident Form ("select affected student(s)") — xem `07_Teacher_Screen_Flow.md`. Ra: Student Detail (tap Profile).
**Bố cục**: AppBar (Home shortcut + tiêu đề) → Profile Filter Tabs → danh sách `Card` cuộn dọc (1 học sinh/card) → `BottomNavigationBar`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student List, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Profile Filter Tabs (All/Allergy Warning/Special Diet/Normal) + danh sách Card cuộn dọc + BottomNavigationBar.
Thành phần bắt buộc: Home Shortcut Button, Screen Title Header, Profile Filter Tabs, Student Record Card (avatar, tên, badge trạng thái, link Profile).
Áp dụng Design System: badge màu theo Design_System.md mục 3 (danger=Allergy Warning, warning=Special Diet, secondary=Normal).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6351–6365 (Student List in Class, field-by-field gốc).
- `03_Design/04_ScreenFlow/07_Teacher_Screen_Flow.md`.
- `business_rules.md` BR-025, BR-030.
