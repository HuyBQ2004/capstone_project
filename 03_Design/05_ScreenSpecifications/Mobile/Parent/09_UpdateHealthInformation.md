# Screen Spec — Update Health Information

**Actor**: Parent | **UC liên quan**: UC-093 - Update Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Cùng UI với `08_SubmitHealthInformation.md` (Update Health, Report 3 dòng 6013–6030) nhưng ở chế độ chỉnh sửa — field điền sẵn dữ liệu hiện có. Sau khi lưu, hồ sơ chuyển lại "Pending Verification" (UC-093 bước 5).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Height Input Field | Input số (pre-filled) | Bắt buộc | — | |
| 2 | Weight Input Field | Input số (pre-filled) | Bắt buộc | — | |
| 3 | Blood Type Dropdown | Dropdown (pre-filled) | Tuỳ chọn | — | |
| 4 | File Upload Field | File upload (hiện file cũ nếu có) | Tuỳ chọn | BR-013 | MSG-008/009 |
| 5 | Save Health Info Button | Button (primary, full-width) | Validate, lưu Pending Verification | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải dữ liệu cũ; Spinner khi submit.
- Empty: không áp dụng.
- Error: `SnackBar` `danger` + MSG-004/005/008/009.
- Success: `SnackBar` `success` (MSG-036).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Student Health Information ("Manage Health Details", khi đã có dữ liệu). Ra: về Student Health Information.
**Bố cục**: Giống `08_SubmitHealthInformation.md` nhưng field đã điền sẵn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Health Information, nền tảng Mobile, actor Parent.
Bố cục: AppBar + form 1 cột đã điền sẵn (Height, Weight, Blood Type, File Upload) + nút Save Health Info cố định đáy.
Thành phần bắt buộc: Height Input Field, Weight Input Field, Blood Type Dropdown, File Upload Field, Save Health Info Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/008/009), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6013–6030 (wireframe gốc dùng chung), dòng 2109–2129 (UC-093).
- `business_rules.md` BR-031; `error_message.md` MSG-036.
