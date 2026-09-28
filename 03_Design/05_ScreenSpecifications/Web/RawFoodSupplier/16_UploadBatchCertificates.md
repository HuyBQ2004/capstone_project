# Screen Spec — Upload Batch Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-100 - Upload Batch Certificates (Report 3 §3.2, dòng 3331–3351) — extends UC-043 (Manage Ingredient Batches) | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-009 (Unsupported file type)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Upload Batch Certificates" | — | UC-100 Normal Flow bước 1 |
| 2 | Lô hàng đang chọn (chỉ đọc) | Text/Badge | Tên/mã lô kế thừa từ Batch Detail (14) | BR-071 | |
| 3 | File chứng chỉ (PDF/image) | File upload | Bắt buộc, PDF/JPG/PNG, ≤10MB | BR-071 | MSG-009 nếu sai định dạng |
| 4 | Ngày hết hạn chứng chỉ | Date picker | Bắt buộc | BR-071 | |
| 5 | "Submit"/"Save" Button | Button | Validate định dạng + liên kết chứng chỉ vào lô | BR-071 | |
| 6 | "Cancel" Button | Button | Huỷ, về Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Skeleton form.
- Empty: N/A.
- Error: MSG-009.
- Success: Liên kết chứng chỉ vào lô thành công → về Batch Detail (14), cập nhật danh sách chứng chỉ liên kết.

---

## Platform Layout — Web
**Entry/Exit points**: Từ Batch Detail (14) qua nút "Upload Batch Certificates". Điều hướng đi: quay lại Batch Detail (14).
**Bố cục**: Form ngắn trong `Dialog`/`Sheet` (`Layout_Guidelines.md` §1.2).

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Upload Batch Certificates, nền tảng Web, actor Raw Food Supplier.
Bố cục: Dialog/Sheet form ngắn.
Component bắt buộc dùng: Dialog, file upload + preview, DatePicker, Button.
Thành phần bắt buộc: lô hàng đang chọn (chỉ đọc), file chứng chỉ, ngày hết hạn, nút Submit/Cancel.
Áp dụng Design System: primary #0E7490.
Trạng thái cần thể hiện: error (MSG-009), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3331–3351 (UC-100).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-009.
