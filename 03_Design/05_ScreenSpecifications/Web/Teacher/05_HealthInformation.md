# Screen Spec — Health Information

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info | **Status**: Draft
**BR liên quan**: BR-030, BR-172
**MSG liên quan**: MSG-004, MSG-005

> Bản Web, chỉ đọc — dùng chung field/BR/MSG với bản Mobile. Xem ghi chú giả định ở bản Mobile.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Họ tên, lớp | BR-030 | |
| 2 | Blood Type Card | Metric tile (chỉ đọc) | Nhóm máu | BR-030 | [Giả định] |
| 3 | Chronic Conditions List | Table/List (chỉ đọc) | Bệnh mãn tính/lưu ý y tế | BR-030 | |
| 4 | Emergency Contact Note | Text (chỉ đọc) | Ghi chú liên hệ khẩn | — | [Giả định] |
| 5 | Read-only Notice | Alert (info) | Quyền chỉnh sửa thuộc Medical Staff/Phụ huynh | BR-172 | |

### Trạng thái màn hình
- Loading: `Skeleton`.
- Empty: "Chưa có thông tin sức khỏe được khai báo".
- Error: `Alert variant="destructive"` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Student Detail (tab Health Information).
**Bố cục**: Topbar + Sidebar + Content: Student Identity Header + Blood Type Card + Chronic Conditions Table + Emergency Contact Note + Alert read-only.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Health Information, nền tảng Web, actor Teacher.
Bố cục: Topbar + Sidebar + Student Identity Header + Card nhóm máu + Table bệnh mãn tính + note liên hệ khẩn + Alert.
Thành phần bắt buộc: Student Identity Header, Blood Type Card, Chronic Conditions List, Emergency Contact Note, Read-only Notice.
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm field/nút chỉnh sửa.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5989–6011 (đối chiếu cấu trúc, bản Parent).
- `traceability_matrix.md` §1.3 UC-145.
- `business_rules.md` BR-030, BR-172.
