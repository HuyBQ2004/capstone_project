# Screen Spec — Update Health Information

**Actor**: Parent | **UC liên quan**: UC-093 - Update Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Chế độ chỉnh sửa — dùng chung UI với `08_SubmitHealthInformation.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Height Input Field | `Input` (pre-filled) | Bắt buộc | — | |
| 2 | Weight Input Field | `Input` (pre-filled) | Bắt buộc | — | |
| 3 | Blood Type Select | `Select` (pre-filled) | Tuỳ chọn | — | |
| 4 | File Upload Field | `Input type="file"` (hiện file cũ) | Tuỳ chọn | BR-013 | |
| 5 | Save Health Info Button | Button (primary) | Validate, lưu Pending Verification | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải dữ liệu cũ; Spinner khi submit.
- Error: `Alert variant="destructive"` + MSG-004/005/008/009.
- Success: `Toast` `success` (MSG-036).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng", từ Student Health Information.
**Bố cục**: `Dialog`/`Sheet` ngắn, field điền sẵn dữ liệu hiện có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Health Information, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với form đã điền sẵn (Height, Weight, Blood Type, File Upload) + nút Save.
Thành phần bắt buộc: Height Input Field, Weight Input Field, Blood Type Select, File Upload Field, Save Health Info Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6013–6030, dòng 2109–2129 (UC-093).
- `business_rules.md` BR-031; `error_message.md` MSG-036.
