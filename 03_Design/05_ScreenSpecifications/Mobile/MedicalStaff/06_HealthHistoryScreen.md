# Screen Spec — Health History Screen

**Actor**: Medical Staff | **UC liên quan**: UC-146 (Report 3 §3.2, dòng 2219–2234) | **Status**: Draft
**BR liên quan**: BR-172 (audit trail bất biến — timeline chỉ đọc, không sửa/xóa)
**MSG liên quan**: MSG-004 (Error), MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Student Name & Class (context) | Text | — | — | Kế thừa từ `04_StudentHealthRecordDetail.md` |
| 2 | Timeline sự kiện sức khỏe/dị ứng/khai báo | Timeline (đọc) | Chỉ đọc, không sửa/xóa | BR-172 | UC-146 bước 2–3 |
| 3 | Mỗi mốc thời gian (event) | Card/Item | — | — | Loại sự kiện (khai báo/xác nhận/sự cố), ngày giờ, người thực hiện |
| 4 | Nút "Export" | Button | — | — | Xuất timeline dạng báo cáo (PDF/Excel theo BR-178) |

### Trạng thái màn hình
- Loading: `Skeleton`/`Shimmer` cho timeline.
- Empty: "Chưa có lịch sử sức khỏe nào được ghi nhận" + illustration.
- Error: MSG-004, MSG-005.
- Success: không áp dụng (màn hình xem); xuất báo cáo thành công → Toast xác nhận chung (không có MSG code riêng, dùng theo BR-178).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Health History" trên `04_StudentHealthRecordDetail.md`; Back → quay lại Detail.
**Bố cục**: trang riêng full-screen, AppBar Back, danh sách Timeline dọc (mốc mới nhất trên đầu), nút "Export" cố định góc trên phải AppBar.

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Health History" trên `04_StudentHealthRecordDetail.md` (Web); Back → quay lại Detail.
**Bố cục**: `Sheet`/trang con — Timeline dọc chiếm toàn bộ Content Area, nút "Export" ở góc trên phải page header.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Health History, nền tảng [Web/Mobile], actor Medical Staff.
Bố cục: [lấy từ khối Platform Layout tương ứng ở trên]
Thành phần bắt buộc: Timeline sự kiện sức khỏe/dị ứng/khai báo (chỉ đọc), nút Export.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2219–2234 (UC-146).
- business_rules.md (BR-172, BR-178 xuất báo cáo).
- error_message.md (MSG-004, MSG-005).
