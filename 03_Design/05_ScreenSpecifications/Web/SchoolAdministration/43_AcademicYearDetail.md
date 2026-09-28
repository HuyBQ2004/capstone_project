# Screen Spec — Academic Year Detail

**Actor**: School Administration | **UC liên quan**: bundled (UC-175 Activate/Close, UC-177 Promote Students) | **Status**: Draft
**BR liên quan**: BR-191, BR-193 (đóng năm học → Classroom read-only), BR-195 (Archived vẫn truy vấn được)
**MSG liên quan**: MSG-004, MSG-005, MSG-080, MSG-083

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Year Label, Start/End Date | Text hiển thị | — | — | |
| 2 | Trạng thái hiện tại | Badge (info=Draft, success=Active, secondary=Closed, neutral=Archived) | — | BR-191 | |
| 3 | Danh sách lớp thuộc năm học này | Table | Read-only nếu năm học Closed/Archived | BR-193, BR-195 | |
| 4 | Nút "Activate" | Button (primary) | Chỉ hiện khi Draft; disable + MSG-080 nếu trường đã có năm học Active khác | BR-191 | Mở `Activate/Close Academic Year Dialog` |
| 5 | Nút "Close" | Button (destructive) | Chỉ hiện khi Active | BR-193 | Mở `Activate/Close Academic Year Dialog` |
| 6 | Nút "Promote Students" | Button (secondary) | Chỉ hiện khi năm học Active và có năm học kế tiếp (Draft) tồn tại | BR-194 | Mở `Promote Students Workspace` |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: "Chưa có lớp nào thuộc năm học này" (nếu chưa tạo/sao chép Classroom).
- Error: MSG-080 nếu cố Activate khi đã có năm học Active khác; MSG-004/MSG-005.
- Success: MSG-083 khi Activate/Close thành công.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Academic Year List` (click 1 dòng). Điều hướng ra: `Activate/Close Academic Year Dialog`, `Promote Students Workspace`.
**Bố cục**: Dashboard Shell — Page Header (Year Label + badge trạng thái + nút Activate/Close/Promote góc phải), Content: danh sách lớp thuộc năm học (bảng `Table` toàn bộ chiều rộng).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Academic Year Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (badge trạng thái + nút Activate/Close/Promote Students), bảng danh sách lớp thuộc năm học.
Thành phần bắt buộc: Year Label/Start-End Date, badge trạng thái, bảng lớp học, nút Activate (điều kiện), nút Close (điều kiện), nút Promote Students (điều kiện).
Áp dụng Design System: badge info/success/secondary/neutral, nút Close dùng token danger (hành động rủi ro — khóa Classroom).
Trạng thái cần thể hiện: error (chặn Activate khi đã có năm Active khác), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1865–1929 (Table 3-156 Activate/Close Academic Year, Table 3-158 Promote Students)
- traceability_matrix.md §1.2 (UC-175 ↔ BR-191/BR-193, UC-177 ↔ BR-194)
