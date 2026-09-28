# Screen Spec — Upload Batch Certificates

**Actor**: Raw Food Supplier | **UC liên quan**: UC-100 - Upload Batch Certificates — extends UC-043 | **Status**: Draft
**BR liên quan**: BR-071
**MSG liên quan**: MSG-004, MSG-005, MSG-009

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Screen Title | Text | "Upload Batch Certificates" | — | |
| 2 | Lô hàng đang chọn (chỉ đọc) | Text/Chip | Tên/mã lô kế thừa từ Batch Detail (14) | BR-071 | |
| 3 | File chứng chỉ (PDF/image) | File upload (camera/thư viện) | Bắt buộc, ≤10MB | BR-071 | MSG-009 nếu sai định dạng |
| 4 | Ngày hết hạn chứng chỉ | Date picker | Bắt buộc | BR-071 | |
| 5 | "Submit"/"Save" Button | Button | Validate + liên kết vào lô | BR-071 | |
| 6 | "Cancel" Button | Button | Huỷ, về Batch Detail (14) | — | |

### Trạng thái màn hình
- Loading: Shimmer form.
- Empty: N/A.
- Error: MSG-009.
- Success: Về Batch Detail (14), cập nhật danh sách chứng chỉ.

---

## Platform Layout — Mobile
**Entry/Exit points**: Từ Batch Detail (14) qua nút "Upload Batch Certificates" — tương ứng node `UploadCert` trong `06_Raw_Food_Supplier_Screen_Flow.md` (gọi từ `BatchDetail`). Điều hướng đi: quay lại Batch Detail (14).
**Bố cục**: Modal bottom sheet 1 trang, dùng `image_picker`.

## AI Generation Prompt ⭐
```
Tạo wireframe cho màn hình Upload Batch Certificates, nền tảng Mobile, actor Raw Food Supplier.
Bố cục: Modal bottom sheet form ngắn.
Component bắt buộc dùng: image_picker upload, DatePicker (M3), FilledButton.
Thành phần bắt buộc: lô hàng đang chọn (chỉ đọc), file chứng chỉ, ngày hết hạn, nút Submit/Cancel.
Áp dụng Design System: seed color #0E7490.
Trạng thái cần thể hiện: error (MSG-009), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- `06_Document/01_Report/Report 3.md` dòng 3331–3351 (UC-100).
- `02_Requirements/00_BusinessRules/business_rules.md` BR-071.
- `02_Requirements/00_BusinessRules/error_message.md` MSG-009.
- `03_Design/04_ScreenFlow/06_Raw_Food_Supplier_Screen_Flow.md` (node `UploadCert`, gọi từ `BatchDetail`).
