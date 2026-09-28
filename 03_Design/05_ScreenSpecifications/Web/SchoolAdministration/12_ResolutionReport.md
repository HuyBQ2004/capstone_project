# Screen Spec — Resolution Report

**Actor**: School Administration | **UC liên quan**: UC-013 (Close Incident), UC-121 (View Incident Report) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-155 (đóng sự cố), BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154
**MSG liên quan**: MSG-004, MSG-005, MSG-023, MSG-029 (cập nhật/đóng thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tóm tắt điều tra (root cause) | Text hiển thị | — | BR-150 | |
| 2 | Trạng thái xác minh CAPA hoàn thành | Badge (success/warning) | Chặn đóng nếu còn CAPA chưa hoàn thành (AF1) | BR-152 | |
| 3 | Trạng thái hồi phục của học sinh bị ảnh hưởng | Table/checklist | Toàn bộ phải "Fully Recovered" trước khi đóng | BR-155 | |
| 4 | Closing statement | Textarea | Bắt buộc khi Close Incident | — | |
| 5 | Nút "Close Incident" | Button (primary) | Disable tới khi đủ điều kiện (CAPA hoàn thành + học sinh hồi phục) | BR-155 | |
| 6 | Báo cáo sự cố đầy đủ (clinical notes, lab results, root cause) | Panel/Accordion | — | BR-154 | Nguồn UC-121, chỉ xem |

### Trạng thái màn hình
- Loading: Button "Close Incident" loading khi xử lý.
- Empty: Không áp dụng.
- Error: AF1 UC-013 — còn CAPA chưa hoàn thành → chặn đóng, highlight các CAPA còn mở; MSG-004/MSG-005/MSG-023.
- Success: MSG-029, trạng thái sự cố chuyển "Closed", sinh báo cáo cuối cùng (BR-154), ghi chữ ký điện tử, thông báo các bên liên quan.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident Detail`/`Corrective Action Review` khi đủ điều kiện đóng. Sau khi Close quay về `Incident List`.
**Bố cục**: Dashboard Shell — Page Header (nút Close Incident góc phải, disable tới khi đủ điều kiện), Content: checklist điều kiện đóng (`col-span-6`), panel báo cáo đầy đủ dạng accordion (`col-span-6`), textarea closing statement cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Resolution Report, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Close Incident, disable tới khi đủ điều kiện), checklist điều kiện đóng + panel báo cáo đầy đủ, textarea closing statement.
Thành phần bắt buộc: tóm tắt root cause, trạng thái CAPA, trạng thái hồi phục học sinh, textarea closing statement, nút Close Incident, panel báo cáo đầy đủ (clinical notes/lab results).
Áp dụng Design System: badge success/warning cho điều kiện đóng.
Trạng thái cần thể hiện: error (còn CAPA chưa hoàn thành, chặn đóng), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4357–4373 (Table 3-130 Close Incident), 4617–4635 (Table 3-142 View Incident Report)
- traceability_matrix.md §1.11 (UC-013 ↔ BR-155, UC-121 ↔ BR-154)
