# Screen Spec — Submit Corrective Action Results Form

**Actor**: Service Caterers | **UC liên quan**: UC-082 - Submit Corrective Action Results (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-152, BR-153, BR-155
**MSG liên quan**: MSG-043 (Success), MSG-004 (Error — thiếu Health Department Clearance)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin CAPA đang xử lý | Read-only header | — | — | UC-082 Preconditions |
| 2 | Ghi chú hoàn thành | TextField multiline | Bắt buộc | — | UC-082 Normal Flow bước 2 |
| 3 | Upload tài liệu bằng chứng | Camera/file picker đa file | Bắt buộc ít nhất 1 file | — | UC-082 Normal Flow bước 2 |
| 4 | Giấy chứng nhận Cơ quan Y tế | Upload file | **Bắt buộc** nếu sự cố High Severity từng đóng cửa bếp | BR-153 | UC-082 Exceptions EX1 |
| 5 | Nút "Nộp Kết quả" | FilledButton (cố định cuối màn) | Chặn nếu thiếu Health Department Clearance khi bắt buộc | BR-153 | MSG-004 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin CAPA.
- Empty: Không áp dụng.
- Error: `AlertDialog` MSG-004 (chặn nộp nếu thiếu clearance bắt buộc).
- Success: `SnackBar` xác nhận nộp kết quả thành công.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Corrective Actions Dashboard (nút "Cập nhật Tiến độ"). Thoát: nộp xong quay lại Dashboard.
**Bố cục**: Trang riêng — ghi chú hoàn thành, khối upload tài liệu đa file, khối upload Health Department Clearance (điều kiện), nút nộp cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Corrective Action Results Form, nền tảng Mobile, actor Service Caterers.
Bố cục: ghi chú hoàn thành, upload tài liệu bằng chứng đa file, khối upload Health Department Clearance (điều kiện), nút Nộp Kết quả cố định dưới cùng.
Thành phần bắt buộc: cảnh báo chặn nộp nếu thiếu Health Department Clearance bắt buộc (MSG-004).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn nộp), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4533–4547 (UC-082 - Submit Corrective Action Results)
- business_rules.md BR-152, BR-153, BR-155
