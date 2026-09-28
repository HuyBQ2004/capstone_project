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
| 3 | Mỗi mốc thời gian (event) | Card/Item | — | — | Loại sự kiện, ngày giờ, người thực hiện |
| 4 | Nút "Export" | Button | — | — | Xuất timeline dạng báo cáo (PDF/Excel theo BR-178) |

### Trạng thái màn hình
- Loading: `Skeleton` cho timeline.
- Empty: "Chưa có lịch sử sức khỏe nào được ghi nhận" + illustration.
- Error: MSG-004, MSG-005.
- Success: xuất báo cáo thành công → Toast xác nhận chung (theo BR-178).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ nút "Health History" trên `04_StudentHealthRecordDetail.md` (Web); Back → quay lại Detail.
**Bố cục**: `Sheet`/trang con — Timeline dọc chiếm toàn bộ Content Area, nút "Export" ở góc trên phải page header.

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ nút "Health History" trên `04_StudentHealthRecordDetail.md` (Mobile); Back → quay lại Detail.
**Bố cục**: trang riêng full-screen, AppBar Back, danh sách Timeline dọc, nút "Export" cố định góc trên phải AppBar.

---

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Health History, nền tảng Web, actor Medical Staff.
Bố cục: trang con Timeline dọc chiếm Content Area, nút Export ở page header.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: loading, empty, error.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2219–2234 (UC-146).
- business_rules.md (BR-172, BR-178).
- error_message.md (MSG-004, MSG-005).
