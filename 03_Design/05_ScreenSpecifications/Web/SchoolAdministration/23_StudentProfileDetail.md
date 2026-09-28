# Screen Spec — Student Profile Detail

**Actor**: School Administration | **UC liên quan**: UC-099 (Report 3 §3.2 "Update Student") | **Status**: Draft
**BR liên quan**: BR-025 (sức chứa lớp khi đổi lớp)
**MSG liên quan**: MSG-004, MSG-005, MSG-010 (vượt sức chứa)

> Ghi chú BR: Report 3 gán "Business Rules: BR-041" cho UC-099 (dòng 2169) — BR-041 theo `business_rules.md` hiện hành là "Trạng thái nhà cung cấp" (Supplier Pending/Approved/Rejected), không liên quan ngữ nghĩa tới cập nhật hồ sơ học sinh. Đây là lệch số rõ ràng — dùng BR-025 (sức chứa lớp, khớp đúng Normal Flow bước 4 "system validates capacity") làm căn cứ chính xác hơn, theo nguyên tắc ưu tiên nội dung thật của Normal Flow khi BR code bị lệch.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Họ tên | Input (text) | Bắt buộc | — | |
| 2 | Ngày sinh | DatePicker | Bắt buộc | — | |
| 3 | Lớp (Classroom) | Select | Nếu đổi lớp, lớp mới không được vượt sức chứa 50 (MSG-010) | BR-025 | |
| 4 | School Registration Number / Student Admission Code | Text hiển thị (read-only) | Không cho sửa (mã tự sinh) | — | |
| 5 | Thông tin phụ huynh liên kết | Text hiển thị + link | — | — | Link sang `Parent Account Detail` |
| 6 | Nút "Lưu" | Button (primary) | — | — | |

### Trạng thái màn hình
- Loading: Button "Lưu" loading.
- Empty: Không áp dụng (form pre-filled).
- Error: MSG-010 nếu lớp mới vượt sức chứa; MSG-004/MSG-005.
- Success: Toast xác nhận cập nhật thành công (Report 3 không gán MSG code riêng cho Update Student thành công — dùng MSG-029 chung theo pattern `error_message.md`).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Student Profile Management` (click 1 dòng). Điều hướng ra: `Parent Account Detail`.
**Bố cục**: Dashboard Shell — Page Header (tên học sinh + nút Lưu), form 1 cột nhóm theo section (Thông tin học sinh / Lớp / Phụ huynh liên kết).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Student Profile Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Lưu), form dọc (Thông tin học sinh, Lớp, Phụ huynh liên kết).
Thành phần bắt buộc: Họ tên, Ngày sinh, Lớp (select), mã số read-only, thông tin phụ huynh + link, nút Lưu.
Áp dụng Design System: field read-only dùng màu neutral nhạt.
Trạng thái cần thể hiện: error (vượt sức chứa lớp mới), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2157–2173 (Table 3-40 Update Student Use Cases Specification)
- traceability_matrix.md §1.3 (UC-099)
- business_rules.md BR-025
