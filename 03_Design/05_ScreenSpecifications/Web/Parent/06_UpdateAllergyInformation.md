# Screen Spec — Update Allergy Information

**Actor**: Parent | **UC liên quan**: UC-093 - Update Health & Allergy Information | **Status**: Draft
**BR liên quan**: BR-031
**MSG liên quan**: MSG-004, MSG-005, MSG-036

> Chế độ chỉnh sửa — dùng chung UI với `05_SubmitAllergyInformation.md`. Xem ghi chú đầy đủ ở bản Mobile cùng tên.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Allergen Category Select | `Select` (pre-filled) | Bắt buộc | — | |
| 2 | Specific Allergen Name Input | `Input` (pre-filled, điều kiện) | Bắt buộc nếu Other | — | |
| 3 | Severity Level Radio Group | `RadioGroup` (pre-filled) | Mild/Moderate/Severe | BR-031 | |
| 4 | Upload Medical Proof Component | `Input type="file"` | Bắt buộc nếu chuyển Severe | BR-031 | |
| 5 | Save Allergy Button | Button (primary) | Validate, lưu Pending Verification | BR-031 | MSG-036 |

### Trạng thái màn hình
- Loading: Spinner submit; `Skeleton` khi tải dữ liệu cũ.
- Error: `Alert variant="destructive"` + MSG-004/005.
- Success: `Toast` `success` (MSG-036).
- Empty: không áp dụng.

---

## Platform Layout — Web
**Entry/Exit points**: Sidebar nhóm "Sức khỏe & Dị ứng", từ Student Health Information.
**Bố cục**: `Dialog`/`Sheet` ngắn, field điền sẵn dữ liệu hiện có.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Update Allergy Information, nền tảng Web, actor Parent.
Bố cục: Dialog/Sheet với form đã điền sẵn dữ liệu (Allergen Category, Specific Name, Severity Radio, Upload Medical Proof) + nút Save.
Thành phần bắt buộc: Allergen Category Select, Specific Allergen Name Input, Severity Level Radio Group, Upload Medical Proof Component, Save Allergy Button.
Trạng thái cần thể hiện: loading, error (MSG-004/005), success (MSG-036).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6032–6049, dòng 2109–2129 (UC-093).
- `business_rules.md` BR-031; `error_message.md` MSG-036.
