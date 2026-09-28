# Screen Spec — Submit Health Information

**Actor**: Parent | **UC liên quan**: UC-084 - Submit Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031, BR-172
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Chế độ tạo mới — dùng chung UI với `09_UpdateHealthInformation.md`.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Height Input Field | `Input` số | Bắt buộc | — | |
| 2 | Weight Input Field | `Input` số | Bắt buộc | — | |
| 3 | Blood Type Select | `Select` | Tuỳ chọn | — | |
| 4 | File Upload Field | `Input type="file"` | Tuỳ chọn | BR-013 | MSG-008/009 |
| 5 | Save Health Info Button | Button (primary) | Validate | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: Spinner.
- Error: `Alert variant="destructive"` + MSG-004/005/008/009.
- Success: `Toast` `success` (MSG-036).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng", từ Student Health Information.
**Bố cục**: `Dialog`/`Sheet` ngắn (<8 field) theo `Layout_Guidelines.md` mục 1.2.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Submit Health Information, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với form (Height, Weight, Blood Type Select, File Upload) + nút Save Health Info.
Thành phần bắt buộc: Height Input Field, Weight Input Field, Blood Type Select, File Upload Field, Save Health Info Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005/008/009), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6013–6030, dòng 2087–2107 (UC-084).
- `business_rules.md` BR-013, BR-031, BR-172; `error_message.md` MSG-008, MSG-009, MSG-036.
