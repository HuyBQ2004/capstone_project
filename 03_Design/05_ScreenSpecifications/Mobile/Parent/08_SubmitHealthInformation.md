# Screen Spec — Submit Health Information

**Actor**: Parent | **UC liên quan**: UC-084 - Submit Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031, BR-172
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Đây là màn "Update Health" (Report 3 §3.3.2.4, dòng 6013–6030) ở **chế độ tạo mới (Submit)** — khi phụ huynh khai báo lần đầu (Student Health Information ở trạng thái Empty). Cùng UI với `09_UpdateHealthInformation.md`, khác action lưu (Create vs Update).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Height Input Field | Input số (cm) | Bắt buộc | — | |
| 2 | Weight Input Field | Input số (kg) | Bắt buộc | — | |
| 3 | Blood Type Dropdown | Dropdown | Tuỳ chọn | — | |
| 4 | File Upload Field | File upload (PDF/JPG) | Tuỳ chọn — giấy khám sức khỏe/biểu đồ tăng trưởng | BR-013 | MSG-008/MSG-009 nếu sai định dạng/quá dung lượng |
| 5 | Save Health Info Button | Button (primary, full-width) | Validate Height/Weight bắt buộc | BR-031 | Lưu "Pending Verification", thông báo Medical Staff; MSG-036 |

### Trạng thái màn hình
- Loading: Spinner khi submit.
- Empty: không áp dụng (form tạo mới).
- Error: `SnackBar` `danger` + MSG-004/005/008/009.
- Success: `SnackBar` `success` (MSG-036).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Health Information ("Manage Health Details", khi chưa có dữ liệu). Ra: về Student Health Information.
**Bố cục**: AppBar (back) → form 1 cột: Height → Weight → Blood Type → File Upload → Save Health Info Button cố định đáy.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Health Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + form 1 cột (Height, Weight, Blood Type Dropdown, File Upload) + nút Save Health Info cố định đáy.
Thành phần bắt buộc: Height Input Field, Weight Input Field, Blood Type Dropdown, File Upload Field, Save Health Info Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/008/009), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6013–6030 (Update Health, field-by-field gốc), dòng 2087–2107 (UC-084).
- `business_rules.md` BR-013, BR-031, BR-172; `error_message.md` MSG-008, MSG-009, MSG-036.
