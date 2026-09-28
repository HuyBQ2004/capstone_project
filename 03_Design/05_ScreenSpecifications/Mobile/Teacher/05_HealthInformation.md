# Screen Spec — Health Information

**Actor**: Teacher | **UC liên quan**: UC-145 - View Student Health & Allergy Info | **Status**: Draft
**BR liên quan**: BR-030 (dữ liệu sức khỏe học sinh), BR-172 (bảo vệ toàn vẹn dữ liệu — chỉ đọc phía Teacher)
**MSG liên quan**: MSG-004, MSG-005

> **Ghi chú nguồn**: Report 3 không có wireframe riêng cho Teacher xem "Health Information" (chỉ có bản tương đương phía Parent — "Student Health Hub" — và Medical Staff). Field dưới đây tham chiếu cấu trúc dữ liệu tương tự "Student Health Hub" (Report 3 dòng 5989–6011) nhưng ở chế độ chỉ đọc, thu gọn (Teacher không cần xem cân nặng/chiều cao chi tiết như phụ huynh — chỉ cần thông tin liên quan an toàn thực phẩm/sức khỏe khi có sự cố). [Giả định — cần team xác nhận phạm vi field chính xác Teacher được phép xem].

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Identity Header | Text | Họ tên, lớp | BR-030 | |
| 2 | Blood Type Card | Metric tile (chỉ đọc) | Nhóm máu | BR-030 | [Giả định — cần xác nhận Teacher có quyền xem] |
| 3 | Chronic Conditions List | List (chỉ đọc) | Bệnh mãn tính/lưu ý y tế đã khai báo | BR-030 | |
| 4 | Emergency Contact Note | Text (chỉ đọc) | Ghi chú liên hệ khẩn (nếu có) | — | [Giả định — cần team xác nhận] |
| 5 | Read-only Notice | Banner | "Chỉ Medical Staff/Phụ huynh chỉnh sửa được" | BR-172 | |

### Trạng thái màn hình
- Loading: `Shimmer`.
- Empty: "Chưa có thông tin sức khỏe được khai báo".
- Error: `SnackBar` `danger` + MSG-004/MSG-005.
- Success: không áp dụng.

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Detail ("View Health Information").
**Bố cục**: AppBar (back) → Student Identity Header → Blood Type Card → Chronic Conditions List → Emergency Contact Note → Read-only Notice.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Health Information, nền tảng Mobile, actor Teacher.
Bố cục: AppBar + Student Identity Header + Metric tile nhóm máu + List bệnh mãn tính + note liên hệ khẩn + banner read-only.
Thành phần bắt buộc: Student Identity Header, Blood Type Card, Chronic Conditions List, Emergency Contact Note, Read-only Notice.
Áp dụng Design System: neutral tone (không dùng màu cảnh báo trừ khi có ghi chú nguy hiểm).
Trạng thái cần thể hiện: loading, empty, error (MSG-004/MSG-005).
KHÔNG thêm field/nút chỉnh sửa.
```

## Nguồn dữ liệu
- Report 3.md, dòng 5989–6011 (Student Health Hub, bản Parent — dùng đối chiếu cấu trúc dữ liệu, không phải wireframe gốc của Teacher).
- `traceability_matrix.md` §1.3 UC-145.
- `business_rules.md` BR-030, BR-172.
